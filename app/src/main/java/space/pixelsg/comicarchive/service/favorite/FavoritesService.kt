package space.pixelsg.comicarchive.service.favorite

import kotlinx.coroutines.flow.Flow
import space.pixelsg.comicarchive.models.Comic

interface FavoritesService {
    fun getFavorites(): Flow<List<Comic>>
    suspend fun addFavorites(uri: String, name: String?)
    suspend fun removeFavorites(id: Long)
    suspend fun updatePositions(positions: List<Pair<Long, Int>>)
}