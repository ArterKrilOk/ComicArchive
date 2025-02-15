package space.pixelsg.comicarchive.ui.home.effects

import space.pixelsg.comicarchive.service.favorite.FavoritesService
import space.pixelsg.comicarchive.ui.home.HomeFeature

suspend fun removeFromFavorites(
    eff: HomeFeature.Eff.RemoveComic,
    favoritesService: FavoritesService,
): HomeFeature.Msg.Internal? {
    favoritesService.removeFavorites(eff.id)
    return null
}