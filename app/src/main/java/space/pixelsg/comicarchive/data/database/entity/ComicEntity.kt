package space.pixelsg.comicarchive.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.datetime.Instant
import space.pixelsg.comicarchive.data.database.typeconverters.InstantTypeConverter

@Entity(tableName = "comics", indices = [Index(value = ["uri"], unique = true)])
@TypeConverters(InstantTypeConverter::class)
data class ComicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "index_id")
    val index: Long,
    @ColumnInfo
    val uri: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "position", defaultValue = "0")
    val position: Int,
    @ColumnInfo
    val createdAt: Instant,
    @ColumnInfo
    val updatedAt: Instant,
)