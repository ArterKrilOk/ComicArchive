package space.pixelsg.comicarchive.ui.root

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
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
}