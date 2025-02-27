package space.pixelsg.comicarchive.service.comic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.pixelsg.comicarchive.data.comic.ComicProviderSelector
import space.pixelsg.comicarchive.data.comic.ComicType
import space.pixelsg.comicarchive.data.database.dao.IndexDao
import space.pixelsg.comicarchive.data.database.entity.IndexEntity
import space.pixelsg.comicarchive.data.resolver.UriResolver
import space.pixelsg.comicarchive.data.tmp.TmpFilesProvider
import space.pixelsg.comicarchive.data.tmp.TmpKey
import space.pixelsg.comicarchive.models.ComicInfo
import space.pixelsg.comicarchive.models.TmpFile
import space.pixelsg.comicarchive.utils.ArgumentLock

class ComicServiceImpl(
    private val uriResolver: UriResolver,
    private val indexDao: IndexDao,
    private val comicProviderSelector: ComicProviderSelector,
    private val tmpFilesProvider: TmpFilesProvider,
) : ComicService {
    private val argumentLock = ArgumentLock<String>()
    private val pageArgumentLock = ArgumentLock<String>()

    override suspend fun getComicInfo(uri: String): ComicInfo = withContext(Dispatchers.IO) {
        argumentLock.withLock(uri) {
            // Try to get cached index
            val index = indexDao.getIndex(uri)
            if (index?.pagePaths?.isNotEmpty() == true) return@withLock ComicInfo(
                pages = index.pagePaths,
                type = index.type,
            )
            // Get index data
            val meta = uriResolver.uriMetadata(uri)
            // Select type
            val type = when {
                meta.mime?.contains("zip") == true -> ComicType.Zip
                meta.mime?.contains("pdf") == true -> ComicType.Pdf
                meta.mime?.contains("mng") == true -> ComicType.Mng

                else -> throw IllegalArgumentException("Unsupported comic type")
            }
            // Get pages
            val provider = comicProviderSelector.getComicProvider(type)
            val pages = provider.getComicInfo(uri).pages
            if (pages.isEmpty()) throw RuntimeException("Cant find any comic pages")
            // Cache index
            IndexEntity(
                uri = uri,
                pagePaths = pages,
                type = type,
            ).let { indexDao.insert(it) }
            return@withLock ComicInfo(pages, type)
        }
    }

    override suspend fun getComicPoster(uri: String): TmpFile = withContext(Dispatchers.IO) {
        pageArgumentLock.withLock(uri) {
            // Get info
            val info = getComicInfo(uri)
            val poster = info.pages.first()
            // Try to get tmp
            val tmpFile = tmpFilesProvider.getTmpFile(TmpKey.createFromUriAndPage(uri, poster))
            if (tmpFile != null) return@withLock tmpFile
            // Extract
            val provider = comicProviderSelector.getComicProvider(info.type)
            return@withLock provider.getComicPage(
                uri,
                poster,
                cacheNext = false,
                isPermanent = true,
            )
        }
    }

    override suspend fun getComicPage(uri: String, page: String): TmpFile =
        withContext(Dispatchers.IO) {
            pageArgumentLock.withLock(uri) {
                // Try to get tmp
                val tmpFile = tmpFilesProvider.getTmpFile(TmpKey.createFromUriAndPage(uri, page))
                if (tmpFile != null) return@withLock tmpFile
                // Extract
                val info = getComicInfo(uri)
                val provider = comicProviderSelector.getComicProvider(info.type)
                return@withLock provider.getComicPage(uri, page, cacheNext = true)
            }
        }
}