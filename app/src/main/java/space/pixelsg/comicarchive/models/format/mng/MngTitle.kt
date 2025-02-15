package space.pixelsg.comicarchive.models.format.mng

import kotlinx.serialization.Serializable

@Serializable
data class MngTitle(
    val locale: String,
    val title: String,
)
