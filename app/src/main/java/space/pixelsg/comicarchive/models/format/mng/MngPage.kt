package space.pixelsg.comicarchive.models.format.mng

import kotlinx.serialization.Serializable

@Serializable
data class MngPage(
    val width: Int,
    val height: Int,
    val index: Int,
    val key: String?,
    val source: MngSource,
)
