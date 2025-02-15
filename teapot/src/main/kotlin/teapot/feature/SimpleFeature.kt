package teapot.feature

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
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
import teapot.message.Message
import teapot.reducer.Reducer

class SimpleFeature<S, M : Message, E : Effect>(
    override val featureName: String,
    private val effectHandlers: Set<EffectHandler<E, M>>,
    chatReceivers: Set<ChatReceiver<M>>,
    private val featureScope: CoroutineScope,
    initialState: S,
    messagesBufferCapacity: Int,
    reducer: Reducer<S, M, E>.(state: S, message: M) -> S,
) : Feature<S, M, E> {
    // Get all chats
    private val chats = chatReceivers.map { it.chats }.flatten().toSet()

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