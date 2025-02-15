package teapot.provider

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import teapot.chat.Chat
import teapot.chat.ChatReceiver
import teapot.effect.Effect
import teapot.effect.EffectHandler
import teapot.ext.collectIn
import teapot.feature.Feature
import teapot.message.Message
import teapot.reducer.Reducer

/**
 * FeatureProvider describes how to create a feature. Aka feature blueprint.
 */
abstract class FeatureProvider<S, M : Message, E : Effect>(
    val featureName: String,
) {
    protected open val featureCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
    protected open val messagesBufferCapacity: Int = 1024

    /**
     * Initial state [S] of feature
     */
    protected abstract val initialState: S

    /**
     * Feature reducer. This function will take every new message [M] and turn it into new state [S].
     */
    protected abstract val reducer: Reducer<S, M, E>.(state: S, message: M) -> S

    /**
     * This function will be called in init of the feature. Dangerous API, don't shoot your leg with it.
     */
    protected open val featureInit: Feature<S, M, E>.() -> Unit = { }

    /**
     * Describes how to create your effect handlers [EffectHandler]
     */
    protected open val effectHandlers: () -> Set<EffectHandler<E, M>> = { emptySet() }

    /**
     * Describes how to create your chat receivers [ChatReceiver]
     */
    protected open val chatReceivers: () -> Set<ChatReceiver<M>> = { emptySet() }


    /**
     * Creates new feature instance as described in this Provider
     */
    fun createFeature(): Feature<S, M, E> = object : Feature<S, M, E> {
        override val featureName: String = this@FeatureProvider.featureName

        // Create handlers and receivers
        private val effectHandlers = this@FeatureProvider.effectHandlers()
        private val chatReceivers = this@FeatureProvider.chatReceivers()

        // Get all chats
        private val chats = chatReceivers.map { it.chats }.flatten().toSet()

        private val featureScope =
            CoroutineScope(
                SupervisorJob()
                        + this@FeatureProvider.featureCoroutineDispatcher
                        + CoroutineName(featureName)
            )


        private val _state = MutableStateFlow(initialState)
        override val state: StateFlow<S>
            get() = _state

        private val _messages = MutableSharedFlow<M>(extraBufferCapacity = messagesBufferCapacity)

        override fun dispatch(message: M) {
            featureScope.launch { dispatchSus(message) }
        }

        private suspend fun dispatchSus(message: M) {
            _messages.emit(message)
        }

        private fun launchEffect(effect: E) {
            val effectScope = featureScope + CoroutineExceptionHandler { _, throwable ->
                throwable.printStackTrace()
            }
            effectHandlers.forEach {
                it.handle(effectScope, effect, ::dispatch)
            }
        }

        private fun sendToChat(chat: Chat, message: Message) {
            featureScope.launch { chat.send(message) }
        }

        private val reducerContext = Reducer(
            initialState = initialState,
            dispatchSelfFun = ::dispatch,
            launchEffectFun = ::launchEffect,
            sendFun = { message ->
                chats.forEach {
                    sendToChat(it, message)
                }
            },
            sendToFun = { message, chatKClass ->
                chats.filter {
                    it::class == chatKClass
                }.forEach {
                    sendToChat(it, message)
                }
            },
        )

        override fun clear() {
            featureScope.cancel()
        }

        init {
            // Collect messages and reduce them into new state
            _messages.collectIn(featureScope) { message ->
                _state.emit(reducerContext.reducer(state.first(), message))
            }
            // Redirect all accepted messages to message queue
            chatReceivers.map { it.messagesFlow }.merge().collectIn(featureScope, ::dispatchSus)
        }
    }
}