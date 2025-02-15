package space.pixelsg.comicarchive.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.navOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import teapot.chat.SharedFlowChat
import teapot.message.Message

class NavigationMessenger : SharedFlowChat() {
    private var bind: Job? = null

    suspend fun bindTo(navControllerProvider: () -> NavController) {
        bind?.cancelAndJoin()
        coroutineScope {
            bind = launch {
                messageFlow.filterIsInstance<NavMsg>().collect {
                    when (it) {
                        is NavMsg.NavigateTo -> navControllerProvider().navigate(
                            route = it.destination,
                            navOptions = navOptions {
                                it.popUpTo?.let { route -> popUpTo(route) }
                            },
                        )

                        is NavMsg.PopBack -> navControllerProvider().navigateUp()
                    }
                }
            }
        }
    }
}

sealed interface NavMsg : Message {
    data class NavigateTo(val destination: Destination, val popUpTo: Destination? = null) : NavMsg
    data object PopBack : NavMsg
}