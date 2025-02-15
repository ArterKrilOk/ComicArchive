package space.pixelsg.comicarchive.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
    companion object {
        val default: Destination = Home
    }

    @Serializable
    data object Home : Destination

    @Serializable
    data object Test : Destination

    @Serializable
    data class Reader(val uri: String) : Destination
}