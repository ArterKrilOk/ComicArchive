package space.pixelsg.comicarchive.ui.root

import space.pixelsg.comicarchive.ui.navigation.NavigationMessenger
import space.pixelsg.comicarchive.ui.root.reducers.reduceActions
import space.pixelsg.comicarchive.ui.root.reducers.reduceInternal
import teapot.chat.featureMessageChatReceiver
import teapot.effect.Effect
import teapot.message.Message
import teapot.provider.FeatureP

class RootFeature(
    private val navigationMessenger: NavigationMessenger,
) : FeatureP<RootFeature.State, RootFeature.Msg, RootFeature.Eff>(
    name = "root_feature",
) {

    data class State(
        val isDrawerExpanded: Boolean,
    )

    sealed interface Msg : Message {
        sealed interface Action : Msg {
            data class Drawer(val open: Boolean = true) : Action
        }

        sealed interface Internal : Msg
    }

    sealed interface Eff : Effect

    override fun FeatureTemplate.build() {
        initialState = State(
            isDrawerExpanded = false,
        )

        chatReceivers += featureMessageChatReceiver(navigationMessenger)

        setSimpleReducer { state, message ->
            when (message) {
                is Msg.Action -> reduceActions()(state, message)
                is Msg.Internal -> reduceInternal()(state, message)
            }
        }
    }
}