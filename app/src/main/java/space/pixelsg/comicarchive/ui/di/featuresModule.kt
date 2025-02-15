package space.pixelsg.comicarchive.ui.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import space.pixelsg.comicarchive.ui.home.HomeFeature
import space.pixelsg.comicarchive.ui.navigation.NavigationMessenger
import space.pixelsg.comicarchive.ui.reader.ReaderFeature
import space.pixelsg.comicarchive.ui.root.ContentMessenger
import space.pixelsg.comicarchive.ui.root.RootFeature
import space.pixelsg.comicarchive.ui.test.TestFeature

fun featuresModule() = module {
    singleOf(::NavigationMessenger)
    singleOf(::ContentMessenger)

    factoryOf(::RootFeature)
    factoryOf(::HomeFeature)
    factoryOf(::TestFeature)
    factoryOf(::ReaderFeature)
}