package space.pixelsg.comicarchive.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import space.pixelsg.comicarchive.data.database.entity.TmpEntity

@Dao
interface TmpDao {
    @Insert
    suspend fun insert(entity: TmpEntity): Long

    @Delete
    suspend fun delete(entity: TmpEntity)

    @Query("DELETE FROM tmp_files WHERE id=:id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM tmp_files WHERE id=:id")
    fun entityFlow(id: Long): Flow<TmpEntity?>

    @Query("SELECT * FROM tmp_files WHERE tmp_key=:key ORDER BY permanent DESC")
    suspend fun getEntity(key: String): TmpEntity?

    @Query("SELECT * FROM tmp_files WHERE permanent=0 ORDER BY created_at DESC LIMIT :skip, :limit")
    suspend fun getOldEntries(skip: Int, limit: Int): List<TmpEntity>

    @Query("SELECT * FROM tmp_files")
    suspend fun getAll(): List<TmpEntity>


}