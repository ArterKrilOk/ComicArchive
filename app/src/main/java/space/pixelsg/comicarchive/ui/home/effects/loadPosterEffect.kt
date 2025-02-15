package space.pixelsg.comicarchive.ui.home.effects

import space.pixelsg.comicarchive.service.comic.ComicService
import space.pixelsg.comicarchive.ui.home.HomeFeature

suspend fun loadPosterEffect(
    eff: HomeFeature.Eff.LoadPoster,
    comicService: ComicService,
): HomeFeature.Msg.Internal {
    try {
        val page = comicService.getComicPage(eff.uri, eff.pagePath)
        return HomeFeature.Msg.Internal.PosterLoaded(eff.id, eff.pagePath, page.path)
    } catch (e: Throwable) {
        e.printStackTrace()
        return HomeFeature.Msg.Internal.PosterLoadError(
            eff.id,
            eff.pagePath,
            e.message ?: "Unknown error"
        )
    }
}