package space.pixelsg.comicarchive.ui.home.reducers

import space.pixelsg.comicarchive.ext.updateByPredicate
import space.pixelsg.comicarchive.ui.home.HomeFeature
import space.pixelsg.comicarchive.ui.navigation.Destination
import space.pixelsg.comicarchive.ui.navigation.NavMsg
import space.pixelsg.comicarchive.ui.root.ContentMessenger
import teapot.reducer.simpleReducer

fun reduceActions() =
    simpleReducer<HomeFeature.State, HomeFeature.Msg.Action, HomeFeature.Eff> { state, message ->
        when (message) {
            is HomeFeature.Msg.Action.Init -> initialState.also {
                HomeFeature.Eff.CollectComics.launch()
            }

            is HomeFeature.Msg.Action.PreviewComic -> state.also {
                NavMsg.NavigateTo(Destination.Reader(message.uri)).send()
            }

            is HomeFeature.Msg.Action.AddToFavorites -> state.copy(
                importState = state.importState?.copy(
                    processing = state.importState.processing + message.uris,
                ) ?: HomeFeature.ImportState(
                    processing = message.uris.toSet(),
                    completed = emptySet(),
                )
            ).also {
                message.uris.forEach { uri ->
                    HomeFeature.Eff.AddToFavoritesEff(uri, name = null).launch()
                }
            }

            is HomeFeature.Msg.Action.OpenFileDialog -> state.also {
                ContentMessenger.Msg.GetContents(message.mime).send()
            }

            is HomeFeature.Msg.Action.RemoveComic -> state.also {
                HomeFeature.Eff.RemoveComic(message.id).launch()
            }

            is HomeFeature.Msg.Action.ClearImportState -> state.copy(
                importState = null,
            )

            is HomeFeature.Msg.Action.LoadPoster -> state.copy(
                items = state.items.updateByPredicate(
                    predicate = { it.comic.id == message.id },
                    update = {
                        it.copy(isLoading = true).also { _ ->
                            val posterPath = it.comic.info.pages.firstOrNull()
                            if (!posterPath.isNullOrBlank()) HomeFeature.Eff.LoadPoster(
                                it.comic.id,
                                it.comic.uri,
                                posterPath,
                            ).launch()
                        }
                    }
                )
            )

            is HomeFeature.Msg.Action.Move -> state.copy(
                items = state.items.toMutableList().apply {
                    val fromIndex = indexOfFirst { it.comic.id == message.fromId }
                    val toIndex = indexOfFirst { it.comic.id == message.toId }
                    add(toIndex, removeAt(fromIndex))
                }
            )

            is HomeFeature.Msg.Action.ApplyEditedPositions -> state.also {
                HomeFeature.Eff.ApplyPositions(it.items).launch()
            }
        }
    }