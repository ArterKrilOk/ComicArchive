package space.pixelsg.comicarchive.data.database.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import space.pixelsg.comicarchive.data.database.dao.ComicDao
import space.pixelsg.comicarchive.data.database.dao.IndexDao
import space.pixelsg.comicarchive.data.database.dao.TmpDao
import space.pixelsg.comicarchive.data.database.entity.ComicEntity
import space.pixelsg.comicarchive.data.database.entity.IndexEntity
import space.pixelsg.comicarchive.data.database.entity.TmpEntity

@Database(
    entities = [ComicEntity::class, IndexEntity::class, TmpEntity::class],
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
    ],
    version = 6,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun comicDao(): ComicDao
    abstract fun tmpDao(): TmpDao
    abstract fun indexDao(): IndexDao
}