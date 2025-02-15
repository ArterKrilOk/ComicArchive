package space.pixelsg.comicarchive.models.format.mng

import kotlinx.serialization.Serializable

@Serializable
data class MngIndex(
    val info: MngInfo?,
    val createDate: Long,
    val version: Int,
    val poster: MngPage,
    val pages: List<MngPage>,
)
