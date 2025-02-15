package space.pixelsg.comicarchive.ui.reader.effects

import space.pixelsg.comicarchive.service.comic.ComicService
import space.pixelsg.comicarchive.ui.reader.ReaderFeature

suspend fun openUriEffect(
    eff: ReaderFeature.Eff.OpenUri,
    comicService: ComicService,
): ReaderFeature.Msg.Internal.UriOpened {
    // TODO: Add error handling
    val info = comicService.getComicInfo(eff.uri)
    return ReaderFeature.Msg.Internal.UriOpened(info.pages)
}