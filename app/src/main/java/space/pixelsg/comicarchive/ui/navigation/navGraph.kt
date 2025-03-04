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
import space.pixelsg.comicarchive.ui.settings.SettingsScreen

fun createNavGraph(navController: NavController): NavGraph = navController.createGraph(
    startDestination = Destination.default,
) {
    composable<Destination.Home> {
        HomeScreen(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
        )
    }

    composable<Destination.Settings> {
        SettingsScreen(modifier = Modifier.fillMaxSize(), navController)
    }

    composable<Destination.Reader> {
        ReaderScreen(
            modifier = Modifier.fillMaxSize(),
            destination = it.toRoute<Destination.Reader>(),
        )
    }
}