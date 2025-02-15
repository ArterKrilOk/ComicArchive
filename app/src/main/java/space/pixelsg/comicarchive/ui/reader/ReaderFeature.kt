package space.pixelsg.comicarchive.ui.reader

import space.pixelsg.comicarchive.service.comic.ComicService
import space.pixelsg.comicarchive.ui.navigation.NavigationMessenger
import space.pixelsg.comicarchive.ui.reader.effects.loadPageEffect
import space.pixelsg.comicarchive.ui.reader.effects.openUriEffect
import space.pixelsg.comicarchive.ui.reader.reducer.reduceAction
import space.pixelsg.comicarchive.ui.reader.reducer.reduceInternal
import teapot.chat.sendOnlyChatReceiver
import teapot.effect.Effect
import teapot.effect.simpleSuspendEffectHandler
import teapot.message.Message
import teapot.provider.FeatureP

class ReaderFeature(
    private val navigationMessenger: NavigationMessenger,
    private val comicService: ComicService,
) : FeatureP<ReaderFeature.State, ReaderFeature.Msg, ReaderFeature.Eff>(
    name = "reader_feature"
) {
    data class PageState(
        val isLoading: Boolean,
        val error: String?,
        val errorCount: Int,
        val pagePath: String,
        val imagePath: String?,
    ) {
        val isNeedToLoad: Boolean
            get() = !isLoading && imagePath == null && errorCount < 3
    }

    data class State(
        val uri: String,
        val title: String?,
        val pages: List<PageState>,
        val isLoading: Boolean,
    )

    sealed interface Msg : Message {
        sealed interface Action : Msg {
            data class OpenUri(val uri: String) : Action
            data class LoadPages(val pageIndex: Int, val count: Int) : Action
        }

        sealed interface Internal : Msg {
            data class UriOpened(val pagesPaths: List<String>) : Internal
            data class PageLoaded(val pagePath: String, val imagePath: String) : Internal
            data class PageLoadError(val pagePath: String, val error: String) : Internal
        }
    }

    sealed interface Eff : Effect {
        data class OpenUri(val uri: String) : Eff
        data class LoadPage(val uri: String, val pagePath: String) : Eff
    }

    private fun createEffectsHandler() = simpleSuspendEffectHandler<Eff, Msg.Internal> { eff ->
        when (eff) {
            is Eff.OpenUri -> openUriEffect(eff, comicService)
            is Eff.LoadPage -> loadPageEffect(eff, comicService)
            else -> null
        }
    }

    override fun FeatureTemplate.build() {
        initialState = State(
            uri = "",
            isLoading = false,
            title = null,
            pages = emptyList(),
        )

        chatReceivers += sendOnlyChatReceiver(navigationMessenger)
        effectHandlers += createEffectsHandler()

        setSimpleReducer { state, message ->
            when (message) {
                is Msg.Action -> reduceAction()(state, message)
                is Msg.Internal -> reduceInternal()(state, message)
            }
        }

    }
}