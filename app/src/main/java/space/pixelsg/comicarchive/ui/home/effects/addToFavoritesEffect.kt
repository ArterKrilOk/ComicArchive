package space.pixelsg.comicarchive.ui.home.effects

import space.pixelsg.comicarchive.service.favorite.FavoritesService
import space.pixelsg.comicarchive.ui.home.HomeFeature

suspend fun addToFavoritesEff(
    eff: HomeFeature.Eff.AddToFavoritesEff,
    favoritesService: FavoritesService,
): HomeFeature.Msg.Internal {
    favoritesService.addFavorites(eff.uri, eff.name)
    return HomeFeature.Msg.Internal.AddedToFavorites(eff.uri)
}