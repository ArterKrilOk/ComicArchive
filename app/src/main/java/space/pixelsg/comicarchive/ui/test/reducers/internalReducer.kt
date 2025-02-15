package space.pixelsg.comicarchive.ui.test.reducers

import space.pixelsg.comicarchive.ui.test.TestFeature
import teapot.reducer.simpleReducer

fun reduceInternal() =
    simpleReducer<TestFeature.State, TestFeature.Msg.Internal, TestFeature.Eff> { state, message ->
        state
    }