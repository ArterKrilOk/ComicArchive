package space.pixelsg.comicarchive.data.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import space.pixelsg.comicarchive.data.comic.ComicProviderSelector
import space.pixelsg.comicarchive.data.database.dao.ComicDao
import space.pixelsg.comicarchive.data.database.dao.IndexDao
import space.pixelsg.comicarchive.data.database.dao.TmpDao
import space.pixelsg.comicarchive.data.database.db.AppDatabase
import space.pixelsg.comicarchive.data.resolver.AndroidUriResolver
import space.pixelsg.comicarchive.data.resolver.UriResolver
import space.pixelsg.comicarchive.data.tmp.TmpFilesProvider
import space.pixelsg.comicarchive.data.tmp.TmpFilesProviderImpl
import java.io.File

fun dataModule() = module {
    // Create database
    single<AppDatabase> {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = "app_database"
        ).fallbackToDestructiveMigration().build()
    }
    // Get DAOs
    single<TmpDao> { get<AppDatabase>().tmpDao() }
    single<ComicDao> { get<AppDatabase>().comicDao() }
    single<IndexDao> { get<AppDatabase>().indexDao() }

    single(named("tmp_dir")) {
        File(
            androidContext().cacheDir,
            "tmp"
        ).apply { if (exists().not()) mkdirs() }
    }

    // Create providers
    singleOf(::AndroidUriResolver).bind(UriResolver::class)
    single { ComicProviderSelector(get(), get(), get(), get(named("tmp_dir"))) }
    single<TmpFilesProvider> {
        TmpFilesProviderImpl(get(), get(named("tmp_dir")))
    }

}