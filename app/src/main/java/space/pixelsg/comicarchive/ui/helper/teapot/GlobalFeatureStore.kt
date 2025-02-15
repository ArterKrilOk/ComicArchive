package space.pixelsg.comicarchive.ui.helper.teapot

import teapot.effect.Effect
import teapot.feature.Feature
import teapot.message.Message

object GlobalFeatureStore : FeatureStore {
    private val store = mutableMapOf<Any, Set<Feature<*, *, *>>>()

    override fun <S, M : Message, E : Effect> saveFeature(key: Any, feature: Feature<S, M, E>) {
        val features = store[key] ?: emptySet()
        val sameFeatures = features.filterIsInstance<Feature<S, M, E>>().toSet()
        val newFeatures = features - sameFeatures
        store[key] = newFeatures + feature
    }

    override fun <S, M : Message, E : Effect> restoreFeature(key: Any): Feature<S, M, E>? =
        store.getOrDefault(key, emptySet()).filterIsInstance<Feature<S, M, E>>().firstOrNull()
}