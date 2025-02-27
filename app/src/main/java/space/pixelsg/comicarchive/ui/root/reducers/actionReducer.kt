package space.pixelsg.comicarchive.ui.root.reducers

import space.pixelsg.comicarchive.ui.root.RootFeature
import teapot.reducer.simpleReducer

fun reduceActions() =
    simpleReducer<RootFeature.State, RootFeature.Msg.Action, RootFeature.Eff> { state, message ->
        when (message) {
            is RootFeature.Msg.Action.Drawer -> state.copy(
                isDrawerExpanded = message.open,
            )
        }
    }