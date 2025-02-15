package space.pixelsg.comicarchive.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.datetime.Instant
import space.pixelsg.comicarchive.data.database.typeconverters.InstantTypeConverter

@Entity(tableName = "tmp_files")
@TypeConverters(InstantTypeConverter::class)
data class TmpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "tmp_key")
    val key: String?,
    @ColumnInfo(name = "file_path", defaultValue = "NULL")
    val filePath: String? = null,
    @ColumnInfo(name = "permanent")
    val isPermanent: Boolean,
)
