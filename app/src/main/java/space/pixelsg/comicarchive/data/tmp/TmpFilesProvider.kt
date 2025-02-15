package space.pixelsg.comicarchive.data.tmp

import space.pixelsg.comicarchive.models.TmpFile
import java.io.File
import java.io.OutputStream

interface TmpFilesProvider {
    suspend fun createTmpFileUsingStream(
        key: String?,
        permanent: Boolean = false,
        populateFile: suspend (OutputStream) -> Unit,
    ): TmpFile?

    suspend fun createTmpFile(
        key: String?,
        permanent: Boolean = false,
        openFile: suspend (File) -> Unit,
    ): TmpFile?

    suspend fun createShadowTmpFile(
        key: String?,
        shadowedFileId: Long,
    ): TmpFile?

    suspend fun getTmpFile(key: String): TmpFile?
    suspend fun getTmpFile(id: Long): TmpFile?

    suspend fun clearUnusedTmpFiles()

    suspend fun clearTmpFiles()

}