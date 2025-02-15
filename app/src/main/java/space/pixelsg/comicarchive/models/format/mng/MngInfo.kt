package space.pixelsg.comicarchive.models.format.mng

import kotlinx.serialization.Serializable

@Serializable
data class MngInfo(
    val chapter: Int?,
    val chapterName: String?,
    val volume: Int?,
    val description: String?,
    val titles: List<MngTitle>?,
)
