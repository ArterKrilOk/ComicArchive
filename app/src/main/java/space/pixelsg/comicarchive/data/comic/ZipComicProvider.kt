package space.pixelsg.comicarchive.data.comic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.pixelsg.comicarchive.data.resolver.UriResolver
import space.pixelsg.comicarchive.data.tmp.TmpFilesProvider
import space.pixelsg.comicarchive.data.tmp.TmpKey
import space.pixelsg.comicarchive.models.ComicInfo
import space.pixelsg.comicarchive.models.TmpFile
import java.util.zip.ZipInputStream

class ZipComicProvider(
    private val uriResolver: UriResolver,
    private val tmpFilesProvider: TmpFilesProvider,
) : ComicProvider {
    override suspend fun getComicInfo(uri: String): ComicInfo = withContext(Dispatchers.IO) {
        val pages = mutableListOf<String>()
        uriResolver.openUri(uri) { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    // Get image pages
                    ImageFormats.imageFormats.forEach {
                        if (entry.name.endsWith(it)) pages.add(entry.name)
                    }
                    entry = zis.nextEntry
                }
            }
        }

        val sortedPages = pages.sortedBy {
            val nameDigits = it.filter { c -> c.isDigit() }
            if (nameDigits.isNotEmpty()) nameDigits.toIntOrNull() ?: Int.MAX_VALUE
            else Int.MAX_VALUE
        }

        ComicInfo(
            type = ComicType.Zip,
            pages = sortedPages,
        )
    }

    override suspend fun getComicPage(
        uri: String,
        page: String,
        cacheNext: Boolean,
        isPermanent: Boolean
    ): TmpFile = uriResolver.openUri(uri) { inputStream ->
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            // Skip until correct entry
            while (entry.name != page) entry = zis.nextEntry
            // Extract current page
            val tmpEntity = tmpFilesProvider.createTmpFileUsingStream(
                key = TmpKey.createFromUriAndPage(uri, page),
                permanent = isPermanent,
                populateFile = { fos -> zis.copyTo(fos) },
            ) ?: throw RuntimeException("Cant populate tmp file with $page")

            if (!cacheNext) return@openUri tmpEntity
            // Extract additional pages
            repeat(CACHE_NEXT) {
                entry = zis.nextEntry ?: return@repeat
                tmpFilesProvider.createTmpFileUsingStream(
                    key = TmpKey.createFromUriAndPage(uri, entry.name),
                    populateFile = { fos -> zis.copyTo(fos) },
                )
            }

            return@openUri tmpEntity
        }
    }

    companion object {
        private const val CACHE_NEXT = 10
    }
}