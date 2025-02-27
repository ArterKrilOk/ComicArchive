package space.pixelsg.comicarchive.data.comic

import android.annotation.SuppressLint
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import space.pixelsg.comicarchive.data.database.dao.IndexDao
import space.pixelsg.comicarchive.data.resolver.UriResolver
import space.pixelsg.comicarchive.data.tmp.TmpFilesProvider
import space.pixelsg.comicarchive.data.tmp.TmpKey
import space.pixelsg.comicarchive.models.ComicInfo
import space.pixelsg.comicarchive.models.TmpFile
import space.pixelsg.comicarchive.models.format.mng.MngIndex
import space.pixelsg.comicarchive.models.format.mng.MngPage
import java.io.File
import java.util.zip.ZipInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MngComicProvider(
    private val uriResolver: UriResolver,
    private val tmpFilesProvider: TmpFilesProvider,
    private val indexDao: IndexDao,
    private val cacheDir: File,
) : ComicProvider {
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        useAlternativeNames = true
        prettyPrint = false
        explicitNulls = false
    }

    override suspend fun getComicInfo(uri: String): ComicInfo {
        val index = getIndex(uri)

        val pages = index.pages.map { page -> json.encodeToString(page) }

        return ComicInfo(
            pages = pages,
            type = ComicType.Mng,
        )
    }

    private suspend fun getIndex(path: String): MngIndex = withContext(Dispatchers.IO) {
        val indexJson = uriResolver.openUri(path) { uis ->
            if (uis == null) throw RuntimeException("Mng not found")

            ZipInputStream(uis).use { zip ->
                var entry = zip.nextEntry
                // Search for index
                while (entry.name != "index.json") entry = zip.nextEntry

                zip.bufferedReader().use { it.readText() }
            }
        }

        val index = json.decodeFromString<MngIndex>(indexJson)

        return@withContext index
    }

    override suspend fun getComicPage(
        uri: String,
        page: String,
        cacheNext: Boolean,
        isPermanent: Boolean
    ): TmpFile {
        val mngPage: MngPage = json.decodeFromString(page)
        // Try to get all pages
        val pages = indexDao.getIndex(uri)?.pagePaths?.map { json.decodeFromString<MngPage>(it) }
            ?: if (cacheNext) getIndex(uri).pages else emptyList()

        val allSources = pages.map { it.source }.toSet()

        // Cache sources
        uriResolver.openUri(uri) { uis ->
            ZipInputStream(uis).use { zis ->
                val currentSourceIndex = allSources.indexOf(mngPage.source)
                val sourcesToCache = mutableSetOf<String>()

                for (i in currentSourceIndex until allSources.size) {
                    if (sourcesToCache.size > CACHE_NEXT_SOURCES) break
                    sourcesToCache += allSources.elementAt(i).name
                }

                var entry = zis.nextEntry
                while (entry != null) {
                    if (sourcesToCache.isEmpty()) break

                    if (entry.name in sourcesToCache) {
                        val tmpKey = TmpKey.createFromUriAndSource(uri, entry.name)
                        val exitingTmp = tmpFilesProvider.getTmpFile(tmpKey)
                        if (exitingTmp == null) tmpFilesProvider.createTmpFileUsingStream(
                            key = tmpKey,
                            populateFile = { fos -> zis.copyTo(fos) },
                        )
                        sourcesToCache -= entry.name
                    }
                    entry = zis.nextEntry
                }
            }
        }

        // Get page source
        val sourceTmp =
            tmpFilesProvider.getTmpFile(TmpKey.createFromUriAndSource(uri, mngPage.source.name))
                ?: throw RuntimeException("Cant get the source for requested page")

        return when (mngPage.source.format) {
            "jpeg", "jpg", "png", "webp", "bmp" -> createImageSourcePage(uri, sourceTmp, page)
            "mp4" -> createVideoSourcePage(uri, sourceTmp, mngPage, page)
            else -> throw RuntimeException("Unsupported source format: ${mngPage.source.format}")
        }
    }

    private suspend fun createImageSourcePage(
        uri: String,
        sourceFile: TmpFile,
        pageKey: String
    ): TmpFile = tmpFilesProvider.createShadowTmpFile(
        key = TmpKey.createFromUriAndPage(uri, pageKey),
        shadowedFileId = sourceFile.tmpId,
    ) ?: throw RuntimeException("Failed to create image source page")

    @SuppressLint("DefaultLocale")
    private suspend fun createVideoSourcePage(
        uri: String,
        tmpSourceFile: TmpFile,
        page: MngPage,
        pageKey: String,
    ): TmpFile {
        val tmpPageFile = File(cacheDir, "${Clock.System.now()}.jpg")
        //ffmpeg -i 000001.mp4 -vf "select=eq(n\,0)" -frames:v 1 -y out0.jpg
        val time = (page.key?.toInt() ?: 0) / 25f
        val timeSS = String.format("%.3f", time) // Format to 3 decimal places
        val args = listOf(
            "-ss",
            timeSS,
            "-i",
            tmpSourceFile.path,
            "-frames:v",
            "1",
            "-y",
            tmpPageFile.absolutePath,
        )
        // Extract frame using FFmpeg
        ffmpegExecuteSus(args.joinToString(" "))

        return tmpFilesProvider.createTmpFileUsingStream(
            key = TmpKey.createFromUriAndPage(uri, pageKey),
            populateFile = { fos ->
                tmpPageFile.inputStream().use { fis ->
                    fis.copyTo(fos)
                }
                tmpPageFile.delete()
            }
        ) ?: throw RuntimeException("Failed to create video source page")
    }


    private suspend fun ffmpegExecuteSus(command: String) =
        suspendCancellableCoroutine { continuation ->
            val session = FFmpegKit.executeAsync(command) {
                if (ReturnCode.isSuccess(it.returnCode)) continuation.resume(it)
                else continuation.resumeWithException(RuntimeException("FFmpeg error: ${it.failStackTrace}"))
            }
            continuation.invokeOnCancellation { session.cancel() }
        }

    companion object {
        private const val CACHE_NEXT_SOURCES = 10
    }
}