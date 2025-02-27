package space.pixelsg.comicarchive.service.torrent

import kotlinx.coroutines.flow.Flow

interface TorrentService {
    fun addTorrent(uri: String)

    fun doAction(id: String, action: TorrentAction)
    fun getStatus(): Flow<List<TorrentStatus>>
}

sealed interface TorrentStatus {
    val id: String
    val name: String

    data class Error(
        override val id: String,
        override val name: String,
        val t: Throwable,
    ) : TorrentStatus

    data class Loading(
        override val id: String,
        override val name: String,
        val progress: Float,
    ) : TorrentStatus

    data class Completed(
        override val id: String,
        override val name: String,
    ) : TorrentStatus
}

sealed interface TorrentAction {
    data object Pause : TorrentAction
    data object Resume : TorrentAction
}