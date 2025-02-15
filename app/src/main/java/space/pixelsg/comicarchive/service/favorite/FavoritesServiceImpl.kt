package space.pixelsg.comicarchive.service.favorite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import space.pixelsg.comicarchive.data.database.dao.ComicDao
import space.pixelsg.comicarchive.data.database.dao.IndexDao
import space.pixelsg.comicarchive.data.database.entity.ComicEntity
import space.pixelsg.comicarchive.data.resolver.UriResolver
import space.pixelsg.comicarchive.models.Comic
import space.pixelsg.comicarchive.models.ComicInfo
import space.pixelsg.comicarchive.service.comic.ComicService

class FavoritesServiceImpl(
    private val comicDao: ComicDao,
    private val comicService: ComicService,
    private val uriResolver: UriResolver,
    private val indexDao: IndexDao,
) : FavoritesService {
    override fun getFavorites(): Flow<List<Comic>> =
        comicDao.getComics().flowOn(Dispatchers.IO).map {
            it.map { entity ->
                Comic(
                    id = entity.comic.id,
                    name = entity.comic.title,
                    uri = entity.comic.uri,
                    info = ComicInfo(
                        pages = entity.index.pagePaths,
                        type = entity.index.type,
                    ),
                )
            }
        }

    private val lock = Mutex()


    override suspend fun addFavorites(uri: String, name: String?): Unit =
        withContext(Dispatchers.IO) {
            lock.withLock {
                // Return if already added
                if (comicDao.getComicByUri(uri) != null) return@withContext
                // Get name
                val displayName = name ?: uriResolver.uriMetadata(uri).name ?: "NO NAME"
                // Preload poster and info
                comicService.getComicInfo(uri)
                val indexEntity = indexDao.getIndex(uri) ?: return@withContext
                comicService.getComicPoster(uri)
                // Save instance
                ComicEntity(
                    uri = uri,
                    index = indexEntity.id,
                    title = displayName,
                    description = "",
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                    position = 0,
                ).also { comicDao.insertComic(it) }
            }
        }

    override suspend fun removeFavorites(id: Long) {
        withContext(Dispatchers.IO) {
            val entity = comicDao.getComicById(id) ?: return@withContext
            comicDao.deleteComic(entity)
        }
    }

    override suspend fun updatePositions(positions: List<Pair<Long, Int>>) {
        withContext(Dispatchers.IO) {
            comicDao.updatePositions(positions)
        }
    }
}