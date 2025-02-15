package teapot.effect

import teapot.message.Message

fun <E : Effect, M : Message> simpleSuspendEffectHandler(handle: suspend (effect: E) -> M?) =
    object : SuspendEffectHandler<E, M> {
        override suspend fun handle(effect: E): M? = handle(effect)
    }