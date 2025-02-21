package space.pixelsg.comicarchive.ui.home.effects

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import space.pixelsg.comicarchive.service.favorite.FavoritesService
import space.pixelsg.comicarchive.ui.home.HomeFeature

suspend fun applyEditedPositions(
    eff: HomeFeature.Eff.ApplyPositions,
    favoritesService: FavoritesService,
): HomeFeature.Msg.Internal? {
    val positions = eff.list.mapIndexed { index, comicState ->
        comicState.comic.id to index
    }
    favoritesService.updatePositions(positions)
    Firebase.analytics.logEvent("home_screen_positions_updated") { }
    return null
}