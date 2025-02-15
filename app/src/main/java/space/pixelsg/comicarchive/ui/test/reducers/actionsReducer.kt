package space.pixelsg.comicarchive.ui.test.reducers

import space.pixelsg.comicarchive.ui.navigation.Destination
import space.pixelsg.comicarchive.ui.navigation.NavMsg
import space.pixelsg.comicarchive.ui.test.TestFeature
import teapot.reducer.simpleReducer

fun reduceActions() =
    simpleReducer<TestFeature.State, TestFeature.Msg.Action, TestFeature.Eff> { state, message ->
        when (message) {
            is TestFeature.Msg.Action.Init -> initialState

            is TestFeature.Msg.Action.Increment -> state.copy(counter = state.counter + 1)

            is TestFeature.Msg.Action.NavigateHome -> state.also {
                NavMsg.NavigateTo(Destination.Home).send()
            }
        }
    }