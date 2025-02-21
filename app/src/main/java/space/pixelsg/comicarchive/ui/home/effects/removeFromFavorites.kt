package space.pixelsg.comicarchive.ui.home.effects

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import space.pixelsg.comicarchive.service.favorite.FavoritesService
import space.pixelsg.comicarchive.ui.home.HomeFeature

suspend fun removeFromFavorites(
    eff: HomeFeature.Eff.RemoveComic,
    favoritesService: FavoritesService,
): HomeFeature.Msg.Internal? {
    favoritesService.removeFavorites(eff.id)
    Firebase.analytics.logEvent("comic_removed") { }
    return null
}