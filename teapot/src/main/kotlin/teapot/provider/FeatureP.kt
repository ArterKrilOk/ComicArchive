package teapot.provider

import kotlinx.coroutines.CoroutineScope
import teapot.chat.ChatReceiver
import teapot.effect.Effect
import teapot.effect.EffectHandler
import teapot.feature.Feature
import teapot.feature.SimpleFeature
import teapot.message.Message
import teapot.reducer.Reducer

abstract class FeatureP<S, M : Message, E : Effect>(
    private val name: String,
) {
    protected abstract fun FeatureTemplate.build()

    fun createFeature(
        coroutineScope: CoroutineScope,
    ): Feature<S, M, E> {
        val template = FeatureTemplate()
        template.build()

        return SimpleFeature(
            featureName = name,
            featureScope = coroutineScope,
            initialState = template.initialState!!,
            reducer = template.reducer,
            chatReceivers = template.chatReceivers,
            effectHandlers = template.effectHandlers,
            messagesBufferCapacity = template.messagesBufferCapacity,
        )
    }

    inner class FeatureTemplate {
        lateinit var reducer: Reducer<S, M, E>.(state: S, message: M) -> S
        var initialState: S? = null
        val chatReceivers = mutableSetOf<ChatReceiver<M>>()
        val effectHandlers = mutableSetOf<EffectHandler<E, M>>()
        var messagesBufferCapacity: Int = 1024

        fun setSimpleReducer(reducer: Reducer<S, M, E>.(state: S, message: M) -> S) {
            this.reducer = reducer
        }

        fun setChatReceivers(chatReceiver: Set<ChatReceiver<M>>) {
            chatReceivers.clear()
            chatReceivers.addAll(chatReceiver)
        }

        fun addChatReceiver(chatReceiver: ChatReceiver<M>) {
            chatReceivers.add(chatReceiver)
        }

        fun setEffectHandlers(effectHandler: Set<EffectHandler<E, M>>) {
            effectHandlers.clear()
            effectHandlers.addAll(effectHandler)
        }

        fun addEffectHandler(effectHandler: EffectHandler<E, M>) {
            effectHandlers.add(effectHandler)
        }

        fun EffectHandler<E, M>.addToThisFeature() {
            addEffectHandler(this)
        }

        fun ChatReceiver<M>.addToThisFeature() {
            addChatReceiver(this)
        }
    }
}

