package teapot.reducer

import teapot.effect.Effect
import teapot.message.Message

/**
 * Creates simple reducer
 */
fun <S, M : Message, E : Effect> simpleReducer(
    reducer: Reducer<S, M, E>.(state: S, message: M) -> S,
): Reducer<S, M, E>.(state: S, message: M) -> S = reducer