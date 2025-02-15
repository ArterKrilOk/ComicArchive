package space.pixelsg.comicarchive.models

import kotlinx.datetime.Instant

data class TmpFile(
    val path: String,
    val tmpId: Long,
    val createdAt: Instant,
)
