package space.pixelsg.comicarchive.ui.reader.reducer

import space.pixelsg.comicarchive.ui.reader.ReaderFeature
import teapot.reducer.simpleReducer

fun reduceAction() =
    simpleReducer<ReaderFeature.State, ReaderFeature.Msg.Action, ReaderFeature.Eff> { state, message ->
        when (message) {
            is ReaderFeature.Msg.Action.OpenUri -> state.copy(
                isLoading = true,
                uri = message.uri,
            ).also {
                ReaderFeature.Eff.OpenUri(message.uri).launch()
            }

            is ReaderFeature.Msg.Action.LoadPages -> state.copy(
                pages = state.pages.mapIndexed { index, pageState ->
                    fun ReaderFeature.Msg.Action.LoadPages.range(): IntRange =
                        pageIndex..pageIndex + count

                    if (index in message.range() && pageState.isNeedToLoad) pageState.copy(
                        isLoading = true,
                    ).also {
                        ReaderFeature.Eff.LoadPage(state.uri, pageState.pagePath).launch()
                    }
                    else pageState
                },
            )
        }
    }