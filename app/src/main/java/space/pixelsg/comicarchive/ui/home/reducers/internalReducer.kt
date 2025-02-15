package space.pixelsg.comicarchive.ui.home.reducers

import space.pixelsg.comicarchive.ext.updateByPredicate
import space.pixelsg.comicarchive.ui.home.HomeFeature
import teapot.reducer.simpleReducer

fun reduceInternal() =
    simpleReducer<HomeFeature.State, HomeFeature.Msg.Internal, HomeFeature.Eff> { state, message ->
        when (message) {
            is HomeFeature.Msg.Internal.ComicsUpdate -> state.copy(
                items = message.comics.map { newComic ->
                    val oldInstance = state.items.firstOrNull { it.comic.id == newComic.id }
                    oldInstance?.copy(comic = newComic) ?: HomeFeature.ComicState(
                        comic = newComic,
                        isLoading = false,
                        imagePath = null,
                        error = null,
                    )
                },
            )

            is HomeFeature.Msg.Internal.PosterLoadError -> state.copy(
                items = state.items.updateByPredicate(
                    predicate = { it.comic.id == message.id },
                    update = { it.copy(isLoading = false, imagePath = null, error = message.error) }
                ),
            )

            is HomeFeature.Msg.Internal.PosterLoaded -> state.copy(
                items = state.items.updateByPredicate(
                    predicate = { it.comic.id == message.id },
                    update = {
                        it.copy(
                            isLoading = false,
                            imagePath = message.imagePath,
                            error = null,
                        )
                    }
                ),
            )

            is HomeFeature.Msg.Internal.AddedToFavorites -> state.copy(
                importState = state.importState?.copy(
                    processing = state.importState.processing - message.uri,
                    completed = state.importState.completed + message.uri,
                )
            )
        }
    }