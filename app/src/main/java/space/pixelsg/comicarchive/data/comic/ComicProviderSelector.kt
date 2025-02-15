package space.pixelsg.comicarchive.data.comic

import space.pixelsg.comicarchive.data.database.dao.IndexDao
import space.pixelsg.comicarchive.data.resolver.UriResolver
import space.pixelsg.comicarchive.data.tmp.TmpFilesProvider
import java.io.File

class ComicProviderSelector(
    private val uriResolver: UriResolver,
    private val tmpFilesProvider: TmpFilesProvider,
    private val indexDao: IndexDao,
    private val cacheDir: File,
) {
    fun getComicProvider(type: ComicType) = when {
        type == ComicType.Zip -> ZipComicProvider(uriResolver, tmpFilesProvider)
        type == ComicType.Pdf -> PdfComicProvider(uriResolver, tmpFilesProvider)
        type == ComicType.Mng -> MngComicProvider(uriResolver, tmpFilesProvider, indexDao, cacheDir)
        else -> throw IllegalArgumentException("Unsupported comic type")
    }
}