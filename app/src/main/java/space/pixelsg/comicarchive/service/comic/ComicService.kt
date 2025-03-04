package space.pixelsg.comicarchive.service.comic

import space.pixelsg.comicarchive.models.ComicInfo
import space.pixelsg.comicarchive.models.TmpFile

interface ComicService {
    suspend fun getComicInfo(uri: String): ComicInfo
    suspend fun getComicPoster(uri: String): TmpFile
    suspend fun getComicPage(uri: String, page: String, cacheNext: Boolean = true): TmpFile
}