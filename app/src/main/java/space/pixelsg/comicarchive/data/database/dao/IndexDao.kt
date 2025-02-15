package space.pixelsg.comicarchive.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import space.pixelsg.comicarchive.data.database.entity.IndexEntity

@Dao
interface IndexDao {
    @Insert
    fun insert(index: IndexEntity)

    @Query("SELECT * FROM comics_indexes WHERE uri=:uri")
    suspend fun getIndex(uri: String): IndexEntity?
}