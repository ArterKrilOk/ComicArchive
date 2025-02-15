package teapot.effect

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import teapot.ext.collectIn
import teapot.message.Message

sealed interface FlowEffectAction<T> {
    class Collect<T>(val flow: Flow<T>) : FlowEffectAction<T>

    class Cancel<T> : FlowEffectAction<T>
}

fun <E : Effect, T, M : Message> flowEffectHandler(
    flowAction: (E) -> FlowEffectAction<T>?,
    mapper: (T) -> M?,
): EffectHandler<E, M> = object : EffectHandler<E, M> {
    private var collectingJob: Job? = null

    override fun handle(scope: CoroutineScope, effect: E, onCompletion: (M) -> Unit) {
        when (val action = flowAction(effect)) {
            is FlowEffectAction.Collect<T> -> {
                collectingJob?.cancel()
                collectingJob = action.flow.mapNotNull(mapper).collectIn(scope, onCompletion)
            }

            is FlowEffectAction.Cancel<T> -> collectingJob?.cancel()

            else -> Unit
        }
    }
}