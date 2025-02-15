package space.pixelsg.comicarchive.data.comic

import space.pixelsg.comicarchive.models.ComicInfo
import space.pixelsg.comicarchive.models.TmpFile

interface ComicProvider {
    suspend fun getComicInfo(uri: String): ComicInfo

    suspend fun getComicPage(
        uri: String,
        page: String,
        cacheNext: Boolean = false,
        isPermanent: Boolean = false,
    ): TmpFile
}