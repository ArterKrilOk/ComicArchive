package space.pixelsg.comicarchive.ui.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.recordException
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.pixelsg.comicarchive.data.resolver.UriResolver
import space.pixelsg.comicarchive.ext.updateItemByPredicate
import space.pixelsg.comicarchive.models.Comic
import space.pixelsg.comicarchive.service.comic.ComicService
import space.pixelsg.comicarchive.service.favorite.FavoritesService

class HomeViewModel(
    private val comicService: ComicService,
    private val favoritesService: FavoritesService,
    private val uriResolver: UriResolver,
) : ViewModel() {
    // Comic items state
    private val _items = MutableStateFlow<List<ComicState>>(emptyList())
    val items = _items.asStateFlow()

    // Screen Compose states
    private val _isEditMode = mutableStateOf(false)
    val isEditMode: State<Boolean> = _isEditMode

    // Snackbar channel
    private val _snackChannel = Channel<HomeSnackBar>()
    val snacksFlow = _snackChannel.receiveAsFlow()

    init {
        // Get all updates from favorites
        favoritesService.getFavorites()
            .onEach { updateCurrentItems(it) }
            .launchIn(viewModelScope)

        // Observe items changes
        _items
            .onEach { items ->
                // Exit edit mode then no items available
                if (items.isEmpty()) _isEditMode.value = false
            }
            .launchIn(viewModelScope)
    }

    // Updates current items state and adds new items if needed
    private fun updateCurrentItems(newItems: List<Comic>) {
        _items.update { currentItems ->
            newItems.map { newItem ->
                val oldInstance = currentItems.firstOrNull { it.comic.id == newItem.id }
                oldInstance?.copy(comic = newItem) ?: ComicState(
                    comic = newItem,
                    isLoading = false,
                    imagePath = null,
                    error = null,
                )
            }
        }
    }

    fun enterEditMode() {
        _isEditMode.value = true
    }

    fun exitEditMode() {
        _isEditMode.value = false
        applyItemsPositions()
    }

    // Tries to add uris to comics
    fun addUris(uris: List<String>) {
        viewModelScope.launch {
            uris.forEach { uri ->
                val displayName = uriResolver.uriMetadata(uri).name ?: "NO NAME"

                try {
                    favoritesService.addFavorites(uri, displayName)
                    Firebase.analytics.logEvent("comic_added") {
                        param("name", displayName)
                    }
                    _snackChannel.send(HomeSnackBar.ComicAdded(displayName))
                } catch (t: Throwable) {
                    t.printStackTrace()
                    Firebase.crashlytics.recordException(t) {
                        key("place", "effect")
                        key("action", "add_to_favorites")
                        key("screen", "home_screen")
                    }
                    _snackChannel.send(HomeSnackBar.ComicAddFailed(displayName))
                }
            }
        }
    }

    // Move item inside the list
    fun moveItems(fromComicId: Long, toComicId: Long) {
        _items.update { items ->
            val fromItemIndex = items.indexOfFirst { it.comic.id == fromComicId }
            val toItemIndex = items.indexOfFirst { it.comic.id == toComicId }
            // One of indexes not found, cant move
            if (fromItemIndex == -1 || toItemIndex == -1) return@update items
            // Move items
            items.toMutableList().apply {
                add(toItemIndex, removeAt(fromItemIndex))
            }
        }
    }

    // Triggers poster loading for specific range
    fun loadPosters(fromIndex: Int, count: Int) {
        _items.update { items ->
            val range = fromIndex..fromIndex + count
            items.mapIndexed { index, comicState ->
                // If item in requested range, trigger poster loading and update state
                if (index in range && comicState.isNeedToLoad) {
                    loadComicPoster(comicState.comic)
                    comicState.copy(
                        isLoading = true,
                    )
                } else comicState
            }
        }
    }

    // Loads comic poster and updates item state
    private fun loadComicPoster(comic: Comic) {
        viewModelScope.launch(CoroutineExceptionHandler { _, t ->
            _items.updateItemByPredicate(
                predicate = { it.comic.id == comic.id },
                update = {
                    it.copy(
                        isLoading = false,
                        error = t.message,
                        imagePath = null,
                    )
                }
            )
            t.printStackTrace()
        }) {
            // Get poster
            val posterTmp = comicService.getComicPoster(comic.uri)
            // Update item state
            _items.updateItemByPredicate(
                predicate = { it.comic.id == comic.id },
                update = {
                    it.copy(
                        isLoading = false,
                        imagePath = posterTmp.path,
                    )
                }
            )
        }
    }

    // Save item positions
    private fun applyItemsPositions() {
        val positions = items.value.mapIndexed { index, comicState ->
            comicState.comic.id to index
        }
        viewModelScope.launch { favoritesService.updatePositions(positions) }
    }

    // Removes comics from favorites
    fun removeComic(comicState: ComicState) {
        viewModelScope.launch {
            favoritesService.removeFavorites(comicState.comic.id)
            val snack = HomeSnackBar.ComicRemoved(
                name = comicState.comic.name,
                action = { viewModelScope.launch { addUris(listOf(comicState.comic.uri)) } },
            )
            _snackChannel.send(snack)
        }
    }
}

sealed interface HomeSnackBar {
    data class ComicAdded(val name: String) : HomeSnackBar
    data class ComicAddFailed(val name: String) : HomeSnackBar
    data class ComicRemoved(val name: String, val action: () -> Unit) : HomeSnackBar
}

@Immutable
data class ComicState(
    val comic: Comic,
    val isLoading: Boolean,
    val imagePath: String?,
    val error: String?,
) {
    val isNeedToLoad: Boolean
        get() = !isLoading && error == null && imagePath == null
}