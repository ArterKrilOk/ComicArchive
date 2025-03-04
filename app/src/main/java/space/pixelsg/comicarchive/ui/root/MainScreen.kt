package space.pixelsg.comicarchive.ui.root

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import space.pixelsg.comicarchive.R
import space.pixelsg.comicarchive.ui.navigation.Destination
import space.pixelsg.comicarchive.ui.navigation.createNavGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun closeDrawer() {
        scope.launch { drawerState.close() }
    }

    val currentNavState by navController.currentBackStackEntryAsState()
    val currentRouteClass = currentNavState?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(IntrinsicSize.Max),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterVertically
                    ),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = stringResource(R.string.comic_archive),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterHorizontally),
                    )

                    NavigationDrawerItem(
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Home, contentDescription = null)
                                Text(text = stringResource(R.string.home))
                            }
                        },
                        selected = currentRouteClass == Destination.Home::class.qualifiedName,
                        onClick = {
                            navController.popBackStack(Destination.Home, inclusive = false)
                            closeDrawer()
                        }
                    )

                    NavigationDrawerItem(
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Settings, contentDescription = null)
                                Text(text = stringResource(R.string.settings))
                            }
                        },
                        selected = currentRouteClass == Destination.Settings::class.qualifiedName,
                        onClick = {
                            navController.navigate(Destination.Settings)
                            closeDrawer()
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }

            }
        },
    ) {
        CompositionLocalProvider(LocalDrawerState provides drawerState) {
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