package space.pixelsg.comicarchive.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import space.pixelsg.comicarchive.data.database.entity.ComicEntity
import space.pixelsg.comicarchive.data.database.entity.ComicIndexEntity

@Dao
interface ComicDao {
    @Insert
    fun insertComic(comic: ComicEntity)

    @Delete
    fun deleteComic(comic: ComicEntity)

    @Transaction
    @Query("SELECT * FROM comics ORDER BY position, createdAt DESC")
    fun getComics(): Flow<List<ComicIndexEntity>>

    @Query("SELECT * FROM comics WHERE id=:id")
    fun getComicById(id: Long): ComicEntity?

    @Query("SELECT * FROM comics WHERE uri=:uri")
    fun getComicByUri(uri: String): ComicEntity?

    @Query("UPDATE comics SET position=:position WHERE id=:id")
    suspend fun updatePosition(id: Long, position: Int)

    @Transaction
    suspend fun updatePositions(positions: List<Pair<Long, Int>>) {
        positions.forEach { (id, position) -> updatePosition(id, position) }
    }
}