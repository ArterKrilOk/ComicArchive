package space.pixelsg.comicarchive.ui.home.effects

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.recordException
import com.google.firebase.ktx.Firebase
import space.pixelsg.comicarchive.service.favorite.FavoritesService
import space.pixelsg.comicarchive.ui.home.HomeFeature

suspend fun addToFavoritesEff(
    eff: HomeFeature.Eff.AddToFavoritesEff,
    favoritesService: FavoritesService,
): HomeFeature.Msg.Internal? {
    try {
        favoritesService.addFavorites(eff.uri, eff.name)
        Firebase.analytics.logEvent("comic_added") {
            param("name", eff.name ?: "NO NAME")
        }
        return HomeFeature.Msg.Internal.AddedToFavorites(eff.uri)
    } catch (t: Throwable) {
        t.printStackTrace()
        Firebase.crashlytics.recordException(t) {
            key("place", "effect")
            key("action", "add_to_favorites")
            key("screen", "home_screen")
        }
        return null
    }
}