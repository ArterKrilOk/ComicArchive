package space.pixelsg.comicarchive.ui.reader.effects

import space.pixelsg.comicarchive.service.comic.ComicService
import space.pixelsg.comicarchive.ui.reader.ReaderFeature

suspend fun loadPageEffect(
    eff: ReaderFeature.Eff.LoadPage,
    comicService: ComicService,
): ReaderFeature.Msg.Internal {
    try {
        val page = comicService.getComicPage(eff.uri, eff.pagePath)
        return ReaderFeature.Msg.Internal.PageLoaded(eff.pagePath, page.path)
    } catch (e: Throwable) {
        e.printStackTrace()
        return ReaderFeature.Msg.Internal.PageLoadError(eff.pagePath, e.message ?: "Unknown error")
    }
}