package space.pixelsg.comicarchive.service.torrent

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import org.libtorrent4j.AlertListener
import org.libtorrent4j.SessionManager
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.alerts.AddTorrentAlert
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.BlockDownloadingAlert
import org.libtorrent4j.alerts.BlockFinishedAlert
import org.libtorrent4j.alerts.FileCompletedAlert
import org.libtorrent4j.alerts.FileProgressAlert
import org.libtorrent4j.alerts.PieceFinishedAlert
import org.libtorrent4j.alerts.TorrentAlert
import org.libtorrent4j.alerts.TorrentErrorAlert
import org.libtorrent4j.alerts.TorrentFinishedAlert
import org.libtorrent4j.alerts.TrackerErrorAlert
import space.pixelsg.comicarchive.R
import space.pixelsg.comicarchive.data.resolver.UriResolver
import space.pixelsg.comicarchive.data.tmp.TmpFilesProvider
import space.pixelsg.comicarchive.ext.updateByPredicate
import java.io.File

class TorrentDownloaderService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val binder = LocalBinder()
    private lateinit var sessionManager: SessionManager
    private val torrentHandles = mutableMapOf<String, TorrentHandle>() // Key is torrent hash

    private val _torrentStatusFlow = MutableStateFlow<List<TorrentState>>(emptyList())
    val torrentStatusFlow: StateFlow<List<TorrentState>> = _torrentStatusFlow

    private val torrentStatusList: List<TorrentState>
        get() = _torrentStatusFlow.value

    private val notificationId = 1
    private val notificationChannelId = "torrent_downloader_channel"

    private val torrentDir: File by inject(named("torrent_dir"))
    private val uriResolver: UriResolver by inject()
    private val tmpFilesProvider: TmpFilesProvider by inject()

    inner class LocalBinder : Binder() {
        fun getService(): TorrentDownloaderService = this@TorrentDownloaderService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundNotification()
        startTorrentSession()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTorrentSession()
        serviceJob.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                notificationChannelId,
                getString(R.string.torrent_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startForegroundNotification() {
        val notification: Notification = createNotification("Torrent Service Started")
        startForeground(notificationId, notification)
    }

    private fun updateForegroundNotification(message: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = createNotification(message)
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle(getString(R.string.downloading_torrents))
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun startTorrentSession() {
        sessionManager = SessionManager()
        sessionManager.startDht()

        sessionManager.alertsFlow()
            .onEach { alert ->
                when (alert) {
                    is AddTorrentAlert -> addNewTorrent(alert)
                    is TorrentAlert<*> -> alert.handle()?.let { updateTorrentState(it) }
                }
            }
            .launchIn(serviceScope)
    }

    private suspend fun addNewTorrent(alert: AddTorrentAlert) {
        val id = alert.params().torrentInfo.infoHash().toHex()
        torrentHandles[id] = alert.handle()

        val info = alert.params().torrentInfo

        val state = TorrentState(
            id = id,
            name = alert.torrentName(),
            numPieces = info.numPieces(),
            pieceSize = info.pieceLength().toLong(),
            type = TorrentComicType.Unknown,
            status = TStatus.Pending,
            files = alert.params().filePriorities().map { it.name },
            path = alert.params().savePath,
            progress = 0f,
        )
        updateStatus(torrentStatusList + state)
    }

    private suspend fun updateTorrentState(handle: TorrentHandle) {
        torrentStatusList.updateByPredicate(
            predicate = { it.id == handle.torrentFile().idHex() },
            update = {
                val progress = handle.status().progress()
                val isFinished = handle.status().isFinished

                it.copy(
                    progress = if (isFinished) 1f else progress,
                    status = when {
                        isFinished -> TStatus.Finished
                        progress >= 0.01f -> TStatus.Downloading
                        else -> TStatus.Pending
                    }
                )
            }
        ).let { updateStatus(it) }
    }

    private suspend fun updateStatus(newStatus: List<TorrentState>) {
        _torrentStatusFlow.emit(newStatus)
        updateNotificationBasedOnStatus()
    }

    private fun TorrentInfo.idHex(): String = infoHash().toHex()

    private fun SessionManager.alertsFlow() = callbackFlow {
        val listener = object : AlertListener {
            override fun types(): IntArray? = null

            override fun alert(alert: Alert<*>?) {
                alert?.let {
                    val convertedAlert = when (alert.type()) {
                        AlertType.ADD_TORRENT -> alert as AddTorrentAlert
                        AlertType.BLOCK_FINISHED -> alert as BlockFinishedAlert
                        AlertType.BLOCK_DOWNLOADING -> alert as BlockDownloadingAlert
                        AlertType.PIECE_FINISHED -> alert as PieceFinishedAlert
                        AlertType.TORRENT_FINISHED -> alert as TorrentFinishedAlert
                        AlertType.TORRENT_ERROR -> alert as TorrentErrorAlert
                        AlertType.TRACKER_ERROR -> alert as TrackerErrorAlert
                        AlertType.FILE_PROGRESS -> alert as FileProgressAlert
                        AlertType.FILE_COMPLETED -> alert as FileCompletedAlert

                        else -> alert
                    }
                    trySendBlocking(convertedAlert)
                }
            }
        }

        sessionManager.addListener(listener)
        awaitClose { removeListener(listener) }
    }

    private fun stopTorrentSession() {
        sessionManager.stopDht()
        sessionManager.pause()
    }


    suspend fun addTorrent(uriString: String) {
        // Copy torrent file to tmp
        val tmpFile = tmpFilesProvider.createTmpFileUsingStream(
            key = "torrent_${Clock.System.now().nanosecondsOfSecond}",
        ) { fos ->
            uriResolver.openUri(uriString) { fis ->
                fis?.copyTo(fos) ?: throw RuntimeException("Cant open torrent file")
            }
        } ?: throw RuntimeException("Failed to create torrent tmp file")
        val torrentFile = File(tmpFile.path)

        // Open torrent file
        val torrentInfo = TorrentInfo(torrentFile)

        // Start download
        sessionManager.download(torrentInfo, torrentDir)
    }

    private fun updateNotificationBasedOnStatus() {
        val downloadingCount = torrentStatusList.count { it.status == TStatus.Downloading }
        if (downloadingCount > 0) {
            updateForegroundNotification("$downloadingCount torrents downloading...")
        } else if (torrentStatusList.any { it.status == TStatus.Failed }) {
            updateForegroundNotification("Some torrents have errors.")
        } else if (torrentStatusList.any { it.status == TStatus.Finished }) {
            updateForegroundNotification("Downloads completed.")
        } else {
            updateForegroundNotification("Torrent Service Started")
        }
    }

    companion object {
        private const val TAG = "TorrentDownloaderService"
    }
}