package space.pixelsg.comicarchive.ui.root.reducers

import space.pixelsg.comicarchive.ui.root.RootFeature
import teapot.reducer.simpleReducer

fun reduceInternal() =
    simpleReducer<RootFeature.State, RootFeature.Msg.Internal, RootFeature.Eff> { state, message ->
        when (message) {
            else -> state
        }
    }