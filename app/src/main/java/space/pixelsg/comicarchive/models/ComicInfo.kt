package space.pixelsg.comicarchive.models

import space.pixelsg.comicarchive.data.comic.ComicType

data class ComicInfo(
    val pages: List<String>,
    val type: ComicType,
)
