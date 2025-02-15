package space.pixelsg.comicarchive.ui.helper.teapot

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import org.koin.android.ext.android.get
import org.koin.compose.getKoin
import teapot.effect.Effect
import teapot.feature.Feature
import teapot.message.Message
import teapot.provider.FeatureP
import kotlin.reflect.KClass

inline fun <reified F : FeatureP<S, M, E>, S, M : Message, E : Effect> ComponentActivity.features(
    kClass: KClass<F>
) = lazy { get<F>().createFeature(lifecycleScope) }

inline fun <reified F : FeatureP<S, M, E>, S, M : Message, E : Effect> Fragment.features(
    kClass: KClass<F>
) = lazy { get<F>().createFeature(lifecycleScope) }

@Composable
inline fun <reified F : FeatureP<S, M, E>, S, M : Message, E : Effect> features(
    kClass: KClass<F>,
    featureStore: FeatureStore = GlobalFeatureStore,
): Feature<S, M, E> {
    val currentCompositionHash = currentCompositeKeyHash

    featureStore.restoreFeature<S, M, E>(currentCompositionHash)?.let { feature ->
        return remember { feature }
    }

    val scope = LocalLifecycleOwner.current.lifecycleScope
    val koin = getKoin()
    return remember {
        koin.get<F>().createFeature(scope).also { feature ->
            featureStore.saveFeature(currentCompositionHash, feature)
        }
    }
}


