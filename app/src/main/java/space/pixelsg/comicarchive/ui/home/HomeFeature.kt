package space.pixelsg.comicarchive.ui.home

import space.pixelsg.comicarchive.models.Comic
import space.pixelsg.comicarchive.service.comic.ComicService
import space.pixelsg.comicarchive.service.favorite.FavoritesService
import space.pixelsg.comicarchive.ui.home.effects.addToFavoritesEff
import space.pixelsg.comicarchive.ui.home.effects.applyEditedPositions
import space.pixelsg.comicarchive.ui.home.effects.loadPosterEffect
import space.pixelsg.comicarchive.ui.home.effects.removeFromFavorites
import space.pixelsg.comicarchive.ui.home.reducers.reduceActions
import space.pixelsg.comicarchive.ui.home.reducers.reduceInternal
import space.pixelsg.comicarchive.ui.navigation.NavigationMessenger
import space.pixelsg.comicarchive.ui.root.ContentMessenger
import teapot.chat.mappingChatReceiver
import teapot.chat.sendOnlyChatReceiver
import teapot.effect.Effect
import teapot.effect.FlowEffectAction
import teapot.effect.flowEffectHandler
import teapot.effect.simpleSuspendEffectHandler
import teapot.message.Message
import teapot.provider.FeatureP

class HomeFeature(
    private val navigationMessenger: NavigationMessenger,
    private val contentMessenger: ContentMessenger,
    private val favoritesService: FavoritesService,
    private val comicService: ComicService,
) : FeatureP<HomeFeature.State, HomeFeature.Msg, HomeFeature.Eff>(
    name = "home_feature",
) {
    data class ComicState(
        val comic: Comic,
        val isLoading: Boolean,
        val imagePath: String?,
        val error: String?,
    ) {
        val isNeedToLoad: Boolean
            get() = !isLoading && error == null && imagePath == null
    }

    data class ImportState(
        val processing: Set<String>,
        val completed: Set<String>,
    ) {
        val itemsCount: Int
            get() = processing.size + completed.size

        val progress: Float
            get() = completed.size.toFloat() / itemsCount
    }

    data class State(
        val items: List<ComicState>,
        val importState: ImportState?,
    )

    sealed interface Msg : Message {
        sealed interface Action : Msg {
            data object Init : Action
            data class OpenFileDialog(val mime: List<String>) : Action
            data class PreviewComic(val uri: String) : Action
            data class AddToFavorites(val uris: List<String>) : Action
            data class RemoveComic(val id: Long) : Action
            data object ClearImportState : Action

            data class LoadPoster(val id: Long) : Action

            data class Move(val fromId: Long, val toId: Long) : Action
            data object ApplyEditedPositions : Action
        }

        sealed interface Internal : Msg {
            data class ComicsUpdate(val comics: List<Comic>) : Internal
            data class AddedToFavorites(val uri: String) : Internal
            data class PosterLoaded(val id: Long, val uri: String, val imagePath: String) : Internal
            data class PosterLoadError(val id: Long, val uri: String, val error: String) : Internal
        }
    }

    sealed interface Eff : Effect {
        data class LoadPoster(val id: Long, val uri: String, val pagePath: String) : Eff
        data class AddToFavoritesEff(val uri: String, val name: String?) : Eff
        data object CollectComics : Eff
        data class RemoveComic(val id: Long) : Eff
        data class ApplyPositions(val list: List<ComicState>) : Eff
    }

    private fun createEffectHandler() = simpleSuspendEffectHandler<Eff, Msg.Internal> { eff ->
        when (eff) {
            is Eff.LoadPoster -> loadPosterEffect(eff, comicService)
            is Eff.AddToFavoritesEff -> addToFavoritesEff(eff, favoritesService)
            is Eff.RemoveComic -> removeFromFavorites(eff, favoritesService)
            is Eff.ApplyPositions -> applyEditedPositions(eff, favoritesService)

            else -> null
        }
    }

    override fun FeatureTemplate.build() {
        initialState = State(
            items = emptyList(),
            importState = null,
        )

        chatReceivers += sendOnlyChatReceiver(navigationMessenger)
        chatReceivers += mappingChatReceiver(
            mapper = {
                when (it) {
                    is ContentMessenger.Msg.GetContentsResults ->
                        if (it.uris.isNotEmpty()) Msg.Action.AddToFavorites(it.uris)
                        else null

                    else -> null
                }
            },
            contentMessenger,
        )

        effectHandlers += createEffectHandler()
        effectHandlers += flowEffectHandler(
            flowAction = {
                if (it is Eff.CollectComics) FlowEffectAction.Collect(favoritesService.getFavorites())
                else null
            },
            mapper = { Msg.Internal.ComicsUpdate(it) }
        )

        setSimpleReducer { state, message ->
            when (message) {
                is Msg.Action -> reduceActions()(state, message)
                is Msg.Internal -> reduceInternal()(state, message)
            }
        }
    }
}