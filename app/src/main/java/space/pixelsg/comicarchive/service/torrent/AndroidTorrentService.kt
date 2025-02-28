package space.pixelsg.comicarchive.service.torrent

import android.app.Notification
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.libtorrent4j.SessionManager
import org.libtorrent4j.Sha1Hash
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import space.pixelsg.comicarchive.R
import java.io.File

class AndroidTorrentService : LifecycleService() {
    private val binder = TorrentBinder()
    private val session = SessionManager()
    private val torrents = mutableMapOf<String, TorrentHandle>()
    private var observeJob: Job? = null
    private val _statusFlow = MutableStateFlow<List<TorrentStatus>>(emptyList())
    val statusFlow: StateFlow<List<TorrentStatus>> get() = _statusFlow

    private val torrentDir by lazy {
        File(cacheDir, "torrent").apply {
            if (!exists()) mkdirs()
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
        session.start()
        observeTorrentStatus()
    }

    override fun onDestroy() {
        observeJob?.cancel()
        session.stop()
        super.onDestroy()
    }

    fun addTorrent(torrentFile: File) {
        val ti = TorrentInfo(torrentFile)
        val hash = ti.infoHash()

        session.download(ti, torrentDir)
        val handle = session.find(hash)
        torrents[hash.toHex()] = handle
    }

    fun doAction(id: String, action: TorrentAction) {
        val handle = session.find(Sha1Hash.parseHex(id))
        handle?.let {
            when (action) {
                is TorrentAction.Pause -> it.pause()
                is TorrentAction.Resume -> it.resume()
            }
        }
    }

    private fun observeTorrentStatus() {
        observeJob?.cancel()
        observeJob = GlobalScope.launch {
            while (true) {
                torrents.forEach { (hash, handle) ->

                }
            }
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "torrent_channel")
            .setContentTitle(getString(R.string.downloading_torrents))
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
    }

    inner class TorrentBinder : Binder() {
        fun getService(): AndroidTorrentService = this@AndroidTorrentService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }
}