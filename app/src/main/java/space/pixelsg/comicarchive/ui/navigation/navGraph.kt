package space.pixelsg.comicarchive.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.toRoute
import space.pixelsg.comicarchive.ui.home.compose.HomeScreen
import space.pixelsg.comicarchive.ui.reader.compose.ReaderScreen
import space.pixelsg.comicarchive.ui.test.TestScreen

fun createNavGraph(navController: NavController): NavGraph = navController.createGraph(
    startDestination = Destination.default,
) {
    composable<Destination.Home> {
        HomeScreen(modifier = Modifier.fillMaxSize())
    }

    composable<Destination.Test> {
        TestScreen(modifier = Modifier.fillMaxSize())
    }

    composable<Destination.Reader> {
        ReaderScreen(
            uri = it.toRoute<Destination.Reader>().uri,
            modifier = Modifier.fillMaxSize(),
        )
    }
}