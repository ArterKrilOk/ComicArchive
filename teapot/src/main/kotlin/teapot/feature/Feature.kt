package teapot.feature

import kotlinx.coroutines.flow.StateFlow
import teapot.effect.Effect
import teapot.message.Message
import teapot.message.MessageDispatcher

/**
 * Heart of Teapot. Combines all single pieces together.
 *
 * Receives messages [M], reduces them and creates new state [S].
 */
interface Feature<S, in M : Message, in E : Effect> : MessageDispatcher<M> {
    /**
     * Name of the feature
     */
    val featureName: String

    /**
     * Feature state flow
     */
    val state: StateFlow<S>

    /**
     * Current feature state. Same as calling [state.value]
     */
    val currentState: S
        get() = state.value

    /**
     * Sends message to feature. Message will be added to message queue and then reduced to new state
     */
    override fun dispatch(message: M)

    /**
     * Cancels feature coroutine scope. You can not use feature after calling this. Dangerous API
     */
    fun clear()
}