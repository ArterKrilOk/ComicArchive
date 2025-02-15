package space.pixelsg.comicarchive.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ComicIndexEntity(
    @Embedded
    val comic: ComicEntity,
    @Relation(
        parentColumn = "index_id",
        entityColumn = "id",
    )
    val index: IndexEntity,
)