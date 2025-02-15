package teapot.effect

import kotlinx.coroutines.CoroutineScope
import teapot.message.Message

/**
 * Describes how [Effect] should be processed.
 *
 * Use [simpleSuspendEffectHandler] for all basic use-cases.
 */
interface EffectHandler<in E : Effect, out M : Message> {
    fun handle(scope: CoroutineScope, effect: E, onCompletion: (M) -> Unit)
}