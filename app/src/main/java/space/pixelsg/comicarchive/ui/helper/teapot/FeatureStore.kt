package space.pixelsg.comicarchive.ui.helper.teapot

import teapot.effect.Effect
import teapot.feature.Feature
import teapot.message.Message

interface FeatureStore {
    fun <S, M : Message, E : Effect> saveFeature(key: Any, feature: Feature<S, M, E>)
    fun <S, M : Message, E : Effect> restoreFeature(key: Any): Feature<S, M, E>?
}