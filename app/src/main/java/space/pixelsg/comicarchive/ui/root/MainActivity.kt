package space.pixelsg.comicarchive.ui.root

import android.animation.ValueAnimator
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.android.inject
import space.pixelsg.comicarchive.ui.helper.teapot.features
import space.pixelsg.comicarchive.ui.navigation.NavigationMessenger
import space.pixelsg.comicarchive.ui.theme.ComicArchiveTheme

class MainActivity : FragmentActivity() {
    private val feature by features(RootFeature::class)
    private val navMessenger by inject<NavigationMessenger>()
    private val contentMessenger by inject<ContentMessenger>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        with(contentMessenger) { bindToActivity() }

        setContent {
            ComicArchiveTheme {
                val state by feature.state.collectAsState()

                MainScreen(
                    navMessenger = navMessenger,
                    state = state,
                    dispatcher = feature,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fixAnimation()
    }

    private fun fixAnimation() {
        val durationScale =
            Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

        if (durationScale != 1f) {
            Firebase.analytics.logEvent("overriding_disabled_animations") {
                param("old_value", durationScale.toDouble())
            }
            try {
                ValueAnimator::class.java.getMethod(
                    "setDurationScale",
                    Float::class.javaPrimitiveType
                ).invoke(null, 1f)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}