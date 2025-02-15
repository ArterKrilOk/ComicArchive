package space.pixelsg.comicarchive.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import space.pixelsg.comicarchive.data.comic.ComicType
import space.pixelsg.comicarchive.data.database.typeconverters.ListTypeConverter

@Entity(
    tableName = "comics_indexes",
    indices = [Index(value = ["uri"], unique = true)],
)
@TypeConverters(ListTypeConverter::class)
data class IndexEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "uri")
    val uri: String,
    @ColumnInfo(name = "pagePaths")
    val pagePaths: List<String>,
    @ColumnInfo(name = "media_type")
    val type: ComicType,
)