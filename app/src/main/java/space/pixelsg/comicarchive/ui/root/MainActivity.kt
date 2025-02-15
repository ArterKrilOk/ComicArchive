package space.pixelsg.comicarchive.ui.root

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.koin.android.ext.android.inject
import space.pixelsg.comicarchive.ui.helper.teapot.features
import space.pixelsg.comicarchive.ui.navigation.NavigationMessenger
import space.pixelsg.comicarchive.ui.navigation.createNavGraph
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
                val navController = rememberNavController()

                LaunchedEffect(navController) {
                    navMessenger.bindTo { navController }
                }

                NavHost(
                    navController = navController,
                    graph = createNavGraph(navController),
                    modifier = Modifier.fillMaxSize(),
                    popExitTransition = {
                        scaleOut(
                            targetScale = 0.85f,
                            animationSpec = spring(stiffness = Spring.StiffnessHigh),
                            transformOrigin = TransformOrigin(
                                pivotFractionX = 0.5f,
                                pivotFractionY = 0.5f
                            )
                        )
                    },
                    popEnterTransition = {
                        EnterTransition.None
                    },
                )
            }
        }
    }
}