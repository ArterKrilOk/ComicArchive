package space.pixelsg.comicarchive.ui.root

import space.pixelsg.comicarchive.ui.navigation.Destination
import space.pixelsg.comicarchive.ui.root.reducers.reduceActions
import space.pixelsg.comicarchive.ui.root.reducers.reduceInternal
import teapot.effect.Effect
import teapot.message.Message
import teapot.provider.FeatureP

class RootFeature(
) : FeatureP<RootFeature.State, RootFeature.Msg, RootFeature.Eff>(
    name = "root_feature",
) {

    data class State(
        val destination: Destination,
    )

    sealed interface Msg : Message {
        sealed interface Action : Msg {
            data class DestinationChanged(val destination: Destination?) : Action
        }

        sealed interface Internal : Msg
    }

    sealed interface Eff : Effect

    override fun FeatureTemplate.build() {
        initialState = State(
            destination = Destination.default,
        )
        setSimpleReducer { state, message ->
            when (message) {
                is Msg.Action -> reduceActions()(state, message)
                is Msg.Internal -> reduceInternal()(state, message)
            }
        }
    }
}