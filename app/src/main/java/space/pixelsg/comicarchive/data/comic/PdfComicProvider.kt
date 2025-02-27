package space.pixelsg.comicarchive.data.comic

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.pixelsg.comicarchive.data.resolver.UriResolver
import space.pixelsg.comicarchive.data.tmp.TmpFilesProvider
import space.pixelsg.comicarchive.data.tmp.TmpKey
import space.pixelsg.comicarchive.models.ComicInfo
import space.pixelsg.comicarchive.models.TmpFile
import java.io.OutputStream
import kotlin.math.min

class PdfComicProvider(
    private val uriResolver: UriResolver,
    private val tmpFilesProvider: TmpFilesProvider,
) : ComicProvider {
    override suspend fun getComicInfo(uri: String): ComicInfo {
        val pages = uriResolver.openUriDescriptor(uri) { pfd ->
            if (pfd == null) throw RuntimeException("Cant open file")

            PdfRenderer(pfd).use { pdfRenderer ->
                List(pdfRenderer.pageCount) { "$it" }
            }
        }

        return ComicInfo(
            type = ComicType.Pdf,
            pages = pages,
        )
    }

    private suspend fun PdfRenderer.renderAndSavePage(page: Int, fos: OutputStream) {
        withContext(Dispatchers.IO) {
            openPage(page).use { page ->
                val bitmap =
                    Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
                bitmap.recycle()
            }
        }
    }

    override suspend fun getComicPage(
        uri: String,
        page: String,
        cacheNext: Boolean,
        isPermanent: Boolean
    ): TmpFile {
        val pageIndex = page.toIntOrNull() ?: throw RuntimeException("PDF page must be a number")

        return uriResolver.openUriDescriptor(uri) { pfd ->
            if (pfd == null) throw RuntimeException("Cant open file")

            PdfRenderer(pfd).use { pdfRenderer ->
                val tmpEntity = tmpFilesProvider.createTmpFileUsingStream(
                    key = TmpKey.createFromUriAndPage(uri, page),
                    permanent = isPermanent,
                    populateFile = { fos -> pdfRenderer.renderAndSavePage(pageIndex, fos) },
                ) ?: throw RuntimeException("Cant populate tmp file with $page")

                val repeatNext = min(CACHE_NEXT, pdfRenderer.pageCount - pageIndex - 1)

                if (cacheNext) repeat(repeatNext) { index ->
                    val tmpKey = TmpKey.createFromUriAndPage(uri, page)
                    val exitingTmp = tmpFilesProvider.getTmpFile(tmpKey)
                    if (exitingTmp == null) tmpFilesProvider.createTmpFileUsingStream(
                        key = tmpKey,
                        permanent = isPermanent,
                        populateFile = { fos ->
                            pdfRenderer.renderAndSavePage(
                                page = pageIndex + index + 1,
                                fos = fos,
                            )
                        },
                    ) ?: throw RuntimeException("Cant populate tmp file with $page")
                }

                return@openUriDescriptor tmpEntity
            }
        }
    }

    companion object {
        private const val CACHE_NEXT = 10
    }
}