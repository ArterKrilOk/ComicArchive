package space.pixelsg.comicarchive.models.format.mng

import kotlinx.serialization.Serializable

@Serializable
data class MngSource(
    val format: String,
    val name: String,
)
