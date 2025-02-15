package space.pixelsg.comicarchive.ui.reader.reducer

import space.pixelsg.comicarchive.ext.updateByPredicate
import space.pixelsg.comicarchive.ui.reader.ReaderFeature
import teapot.reducer.simpleReducer

fun reduceInternal() =
    simpleReducer<ReaderFeature.State, ReaderFeature.Msg.Internal, ReaderFeature.Eff> { state, message ->
        when (message) {
            is ReaderFeature.Msg.Internal.UriOpened -> state.copy(
                pages = message.pagesPaths.map {
                    ReaderFeature.PageState(
                        isLoading = false,
                        error = null,
                        errorCount = 0,
                        pagePath = it,
                        imagePath = null,
                    )
                },
                isLoading = false,
            )

            is ReaderFeature.Msg.Internal.PageLoaded -> state.copy(
                pages = state.pages.updateByPredicate(
                    predicate = { it.pagePath == message.pagePath },
                    update = {
                        it.copy(
                            imagePath = message.imagePath,
                            isLoading = false,
                        )
                    },
                )
            )

            is ReaderFeature.Msg.Internal.PageLoadError -> state.copy(
                pages = state.pages.updateByPredicate(
                    predicate = { it.pagePath == message.pagePath },
                    update = {
                        it.copy(
                            error = message.error,
                            errorCount = it.errorCount + 1,
                            isLoading = false,
                        )
                    },
                )
            )
        }
    }