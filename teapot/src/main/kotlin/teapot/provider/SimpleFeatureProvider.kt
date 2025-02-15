package teapot.provider

import kotlinx.coroutines.CoroutineDispatcher
import teapot.chat.ChatReceiver
import teapot.effect.Effect
import teapot.effect.EffectHandler
import teapot.feature.Feature
import teapot.message.Message
import teapot.reducer.Reducer

fun <S, M : Message, E : Effect> lazyFeatureProvider(
    featureName: String,
    initialState: S,
    reducer: Reducer<S, M, E>.(state: S, message: M) -> S,
    effectHandlers: Set<EffectHandler<E, M>>? = null,
    effectHandlersFactory: (() -> Set<EffectHandler<E, M>>)? = null,
    chatReceivers: Set<ChatReceiver<M>>? = null,
    featureCoroutineDispatcher: CoroutineDispatcher? = null,
    messagesBufferCapacity: Int? = null,
) = lazy {
    featureProvider(
        featureName,
        initialState,
        reducer,
        effectHandlers,
        effectHandlersFactory,
        chatReceivers,
        featureCoroutineDispatcher,
        messagesBufferCapacity
    )
}

/**
 * @param featureName name of a feature
 * @param initialState initial state [S]
 * @param reducer Reduces incoming messages and old state to the new state
 * @param effectHandlers set of effect handlers for a feature
 * @param effectHandlersFactory describes how to create effect handlers. Consider using [effectHandlers] instead.
 * @param chatReceivers set of chat receivers for a feature
 * @param featureCoroutineDispatcher [CoroutineDispatcher] on which all feature actions will be performed
 * @param messagesBufferCapacity feature messages buffer size. Default size is 1024
 * @param featureInit This function will be called in init of the feature. Dangerous API, don't shoot your leg with it.
 */
fun <S, M : Message, E : Effect> featureProvider(
    featureName: String,
    initialState: S,
    reducer: Reducer<S, M, E>.(state: S, message: M) -> S,
    effectHandlers: Set<EffectHandler<E, M>>? = null,
    effectHandlersFactory: (() -> Set<EffectHandler<E, M>>)? = null,
    chatReceivers: Set<ChatReceiver<M>>? = null,
    featureCoroutineDispatcher: CoroutineDispatcher? = null,
    messagesBufferCapacity: Int? = null,
    featureInit: Feature<S, M, E>.() -> Unit = { },
): FeatureProvider<S, M, E> = object : FeatureProvider<S, M, E>(featureName) {
    override val initialState: S
        get() = initialState
    override val reducer: Reducer<S, M, E>.(state: S, message: M) -> S
        get() = reducer

    override val featureInit: Feature<S, M, E>.() -> Unit
        get() = featureInit

    override val effectHandlers: () -> Set<EffectHandler<E, M>>
        get() = effectHandlersFactory ?: effectHandlers?.let { { it } } ?: super.effectHandlers

    override val chatReceivers: () -> Set<ChatReceiver<M>>
        get() = chatReceivers?.let { { it } } ?: super.chatReceivers

    override val featureCoroutineDispatcher: CoroutineDispatcher
        get() = featureCoroutineDispatcher ?: super.featureCoroutineDispatcher

    override val messagesBufferCapacity: Int
        get() = messagesBufferCapacity ?: super.messagesBufferCapacity
}