package space.pixelsg.comicarchive.ui.home.reducers

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

            is HomeFeature.Msg.Action.LoadPosters -> state.copy(
                items = state.items.mapIndexed { index, comicState ->
                    if (index in message.range && comicState.isNeedToLoad) comicState.copy(
                        isLoading = true,
                    ).also {
                        val posterPath = it.comic.info.pages.firstOrNull()
                        if (!posterPath.isNullOrBlank()) HomeFeature.Eff.LoadPoster(
                            id = it.comic.id,
                            uri = it.comic.uri,
                            pagePath = posterPath,
                        ).launch()
                    }
                    else comicState
                }
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

private val HomeFeature.Msg.Action.LoadPosters.range: IntRange
    get() = fromIndex..fromIndex + count