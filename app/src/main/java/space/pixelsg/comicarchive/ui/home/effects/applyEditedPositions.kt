package space.pixelsg.comicarchive.ui.home.effects

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

    return null
}