package space.pixelsg.comicarchive.ui.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import space.pixelsg.comicarchive.ui.home.HomeViewModel
import space.pixelsg.comicarchive.ui.reader.ReaderViewModel

fun viewModelsModule() = module {
    viewModelOf(::HomeViewModel)
    viewModel { ReaderViewModel(get(), it[0]) }
}