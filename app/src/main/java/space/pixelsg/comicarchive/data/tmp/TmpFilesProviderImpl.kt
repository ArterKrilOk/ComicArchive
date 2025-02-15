package space.pixelsg.comicarchive.data.tmp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import okio.use
import space.pixelsg.comicarchive.data.database.dao.TmpDao
import space.pixelsg.comicarchive.data.database.entity.TmpEntity
import space.pixelsg.comicarchive.models.TmpFile
import java.io.File
import java.io.OutputStream

class TmpFilesProviderImpl(
    private val tmpDao: TmpDao,
    private val cacheDir: File,
) : TmpFilesProvider {
    override suspend fun createTmpFileUsingStream(
        key: String?,
        permanent: Boolean,
        populateFile: suspend (OutputStream) -> Unit
    ): TmpFile? {
        val entityId = tmpDao.insert(
            TmpEntity(
                createdAt = Clock.System.now(),
                key = key,
                isPermanent = permanent,
            )
        )
        try {
            withContext(Dispatchers.IO) {
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val tmpFile = File(cacheDir, entityId.toString())
                tmpFile.outputStream().use {
                    populateFile(it)
                }
            }
            return tmpDao.entityFlow(entityId).firstOrNull()?.toTmpFile()
        } catch (e: Exception) {
            e.printStackTrace()
            tmpDao.deleteById(entityId)
            throw e
        }
    }

    override suspend fun createTmpFile(
        key: String?,
        permanent: Boolean,
        openFile: suspend (File) -> Unit
    ): TmpFile? {
        val entityId = tmpDao.insert(
            TmpEntity(
                createdAt = Clock.System.now(),
                key = key,
                isPermanent = permanent,
            )
        )
        try {
            withContext(Dispatchers.IO) {
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val tmpFile = File(cacheDir, entityId.toString())
                openFile(tmpFile)
            }
            return tmpDao.entityFlow(entityId).firstOrNull()?.toTmpFile()
        } catch (e: Exception) {
            e.printStackTrace()
            tmpDao.deleteById(entityId)
            throw e
        }
    }

    override suspend fun createShadowTmpFile(key: String?, shadowedFileId: Long) =
        withContext(Dispatchers.IO) {
            val shadowedEntity = tmpDao.entityFlow(shadowedFileId).firstOrNull()
                ?: throw RuntimeException("Cant find shadowed file")

            val entityId = tmpDao.insert(
                TmpEntity(
                    createdAt = Clock.System.now(),
                    key = key,
                    filePath = shadowedEntity.id.toString(),
                    isPermanent = false,
                )
            )

            return@withContext tmpDao.entityFlow(entityId).firstOrNull()?.toTmpFile()
        }

    override suspend fun getTmpFile(key: String): TmpFile? =
        tmpDao.getEntity(key = key)?.toTmpFile()

    override suspend fun getTmpFile(id: Long): TmpFile? =
        tmpDao.entityFlow(id = id).firstOrNull()?.toTmpFile()

    override suspend fun clearUnusedTmpFiles() {
        deleteEntries(tmpDao.getOldEntries(skip = MAX_CACHE_FILES, limit = 200))
    }

    override suspend fun clearTmpFiles() {
        deleteEntries(tmpDao.getAll())
    }

    private suspend fun deleteEntries(list: List<TmpEntity>) = withContext(Dispatchers.IO) {
        list.forEach {
            it.file.delete()
            tmpDao.delete(it)
        }
    }

    private fun TmpEntity.toTmpFile() = TmpFile(
        path = file.path,
        tmpId = id,
        createdAt = createdAt,
    )

    private val TmpEntity.file: File
        get() = File(cacheDir, filePath ?: id.toString())

    companion object {
        private const val MAX_CACHE_FILES = 200
    }
}