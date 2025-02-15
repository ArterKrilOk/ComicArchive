package teapot.effect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import teapot.message.Message

data class CallbackEffectHandler<M : Message>(val sendMessage: (message: M) -> Unit) {
    fun M.send() = sendMessage(this)
}

fun <E : Effect, M : Message> simpleSuspendCallbackEffectHandler(
    handler: suspend CallbackEffectHandler<M>.(effect: E) -> M?,
) = object : EffectHandler<E, M> {
    override fun handle(scope: CoroutineScope, effect: E, onCompletion: (M) -> Unit) {
        scope.launch {
            CallbackEffectHandler(onCompletion).handler(effect)?.let(onCompletion)
        }
    }
}