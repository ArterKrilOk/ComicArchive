package teapot.effect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import teapot.message.Message

interface SuspendEffectHandler<in E : Effect, out M : Message> : EffectHandler<E, M> {
    suspend fun handle(effect: E): M?

    override fun handle(scope: CoroutineScope, effect: E, onCompletion: (M) -> Unit) {
        scope.launch {
            handle(effect)?.let(onCompletion)
        }
    }
}