package space.pixelsg.comicarchive.service.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import space.pixelsg.comicarchive.service.comic.ComicService
import space.pixelsg.comicarchive.service.comic.ComicServiceImpl
import space.pixelsg.comicarchive.service.favorite.FavoritesService
import space.pixelsg.comicarchive.service.favorite.FavoritesServiceImpl

fun serviceModule() = module {
    singleOf(::ComicServiceImpl).bind(ComicService::class)
    singleOf(::FavoritesServiceImpl).bind(FavoritesService::class)
}