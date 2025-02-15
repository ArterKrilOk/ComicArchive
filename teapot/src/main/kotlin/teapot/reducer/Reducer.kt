package teapot.reducer

import teapot.chat.Chat
import teapot.effect.Effect
import teapot.feature.Feature
import teapot.message.Message
import kotlin.reflect.KClass

class Reducer<out S, in M : Message, in E : Effect>(
    val initialState: S,
    private val dispatchSelfFun: (message: M) -> Unit,
    private val sendFun: (message: Message) -> Unit,
    private val sendToFun: (message: Message, chat: KClass<out Chat>) -> Unit,
    private val launchEffectFun: (effect: E) -> Unit,
) {
    /**
     * Dispatch message [M] to this [Feature]. Do not make loop with it!
     */
    fun dispatchSelf(message: M) = dispatchSelfFun(message)

    /**
     * Send message [Message] to all [Chat]s
     */
    fun sendMessage(message: Message) = sendFun(message)

    /**
     * Send message [Message] to specific [Chat]
     */
    fun sendMessageTo(message: Message, chat: KClass<out Chat>) = sendToFun(message, chat)

    /**
     * Launch [Feature] effect [E]
     */
    fun launchEffect(effect: E) = launchEffectFun(effect)

    fun E.launch() = launchEffect(this)

    fun Message.send() = sendMessage(this)
}