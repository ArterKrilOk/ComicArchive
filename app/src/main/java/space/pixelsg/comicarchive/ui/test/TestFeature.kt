package space.pixelsg.comicarchive.ui.test

import space.pixelsg.comicarchive.ui.navigation.NavigationMessenger
import space.pixelsg.comicarchive.ui.test.reducers.reduceActions
import space.pixelsg.comicarchive.ui.test.reducers.reduceInternal
import teapot.chat.sendOnlyChatReceiver
import teapot.effect.Effect
import teapot.message.Message
import teapot.provider.FeatureP

class TestFeature(
    private val navigationMessenger: NavigationMessenger,
) : FeatureP<TestFeature.State, TestFeature.Msg, TestFeature.Eff>(
    name = "home_feature",
) {
    data class State(
        val counter: Int,
    )

    sealed interface Msg : Message {
        sealed interface Action : Msg {
            data object Init : Action
            data object Increment : Action
            data object NavigateHome : Action
        }

        sealed interface Internal : Msg
    }

    sealed interface Eff : Effect

    override fun FeatureTemplate.build() {
        initialState = State(
            counter = 0
        )

        chatReceivers += sendOnlyChatReceiver(navigationMessenger)

        setSimpleReducer { state, message ->
            when (message) {
                is Msg.Action -> reduceActions()(state, message)
                is Msg.Internal -> reduceInternal()(state, message)
            }
        }
    }
}