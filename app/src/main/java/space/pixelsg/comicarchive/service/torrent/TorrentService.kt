package space.pixelsg.comicarchive.service.torrent

import kotlinx.coroutines.flow.Flow

interface TorrentRepo {
    // Adds torrent using android Uri for torrent file
    fun addTorrent(uri: String)

    // Pauses torrent. ID is torrent hash
    fun pause(id: String)

    // Resumes torrent. ID is torrent hash
    fun resume(id: String)


    fun getStatus(): Flow<List<TorrentState>>
}

data class TorrentState(
    // Torrent hash
    val id: String,
    // Torrent name
    val name: String,
    // Torrent comic type
    val type: TorrentComicType,
    // Contents path
    val path: String,
    // Status
    val status: TStatus,
    // Pieces and files
    val numPieces: Int,
    val pieceSize: Long,
    // Files
    val files: List<String>,
    //
    val progress: Float,
)

enum class TStatus {
    Pending, Downloading, Finished, Failed
}

enum class TorrentComicType {
    Unknown,    // Unknown torrent type
    Archive,    // Torrent contains archive
    Plain,      // Torrent contains individual images (pages)
}