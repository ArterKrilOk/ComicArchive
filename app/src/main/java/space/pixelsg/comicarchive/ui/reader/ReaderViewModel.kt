package space.pixelsg.comicarchive.ui.reader

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.pixelsg.comicarchive.ext.updateItemByPredicate
import space.pixelsg.comicarchive.service.comic.ComicService
import space.pixelsg.comicarchive.ui.navigation.Destination

class ReaderViewModel(
    private val comicService: ComicService,
    destination: Destination.Reader,
) : ViewModel() {
    // Pages state
    private val _pages = MutableStateFlow<List<PageState>>(emptyList())
    val pages = _pages.asStateFlow()

    // Screen state
    private val _screenState = MutableStateFlow(ReaderScreenState())
    val screenState = _screenState.asStateFlow()

    // Comic uri
    private val uri by lazy { destination.uri }

    init {
        // Get info and pages
        viewModelScope.launch { loadInfoAndPages(uri) }
    }

    // Loads comic info and pages
    private suspend fun loadInfoAndPages(uri: String) {
        _screenState.update { state ->
            state.copy(
                isLoading = true,
                title = "",
            )
        }
        val info = comicService.getComicInfo(uri)
        _pages.update {
            info.pages.map { pagePath ->
                PageState(
                    isLoading = false,
                    error = null,
                    errorCount = 0,
                    pagePath = pagePath,
                    imagePath = null,
                )
            }
        }
        _screenState.update { state ->
            state.copy(
                isLoading = false,
                title = "", // TODO: Set title here
            )
        }
    }

    // Triggered by UI then failed to display page
    fun pageFailed(pagePath: String, t: Throwable) {
        _pages.updateItemByPredicate(
            predicate = { it.pagePath == pagePath },
            update = {
                it.copy(
                    isLoading = false,
                    errorCount = it.errorCount + 1,
                    error = t.message,
                ).also { newState ->
                    // Retry loading if attempts left
                    if (newState.isNeedToLoad) loadPage(pagePath, cacheNext = false)
                }
            }
        )
        t.printStackTrace()
    }

    // Load pages
    fun loadPages(index: Int, count: Int) {
        val range = index..index + count
        _pages.update { pages ->
            pages.mapIndexed { index, pageState ->
                if (index in range && pageState.isNeedToLoad) pageState.copy(
                    isLoading = true,
                ).also { loadPage(pageState.pagePath, cacheNext = count != 0) }
                else pageState
            }
        }
    }

    private fun loadPage(pagePath: String, cacheNext: Boolean = true) {
        viewModelScope.launch(CoroutineExceptionHandler { _, t ->
            _pages.updateItemByPredicate(
                predicate = { it.pagePath == pagePath },
                update = {
                    it.copy(
                        isLoading = false,
                        errorCount = it.errorCount + 1,
                        error = t.message,
                    )
                }
            )
            t.printStackTrace()
        }) {
            val pageTmp = comicService.getComicPage(uri, pagePath, cacheNext)
            _pages.updateItemByPredicate(
                predicate = { it.pagePath == pagePath },
                update = {
                    it.copy(
                        isLoading = false,
                        error = null,
                        errorCount = 0,
                        imagePath = pageTmp.path,
                    )
                }
            )
        }
    }

    // Clears page errors and retries loading
    fun retryPage(pagePath: String) {
        _pages.updateItemByPredicate(
            predicate = { it.pagePath == pagePath },
            update = {
                it.copy(
                    errorCount = 0,
                    error = null,
                ).also {
                    loadPage(pagePath, cacheNext = false)
                }
            }
        )
    }
}

@Immutable
data class ReaderScreenState(
    val isLoading: Boolean = false,
    val title: String = "",
)

@Immutable
data class PageState(
    val isLoading: Boolean,
    val error: String?,
    val errorCount: Int,
    val pagePath: String,
    val imagePath: String?,
) {
    val isNeedToLoad: Boolean
        get() = !isLoading && imagePath == null && errorCount < 3

    val isRetryVisible: Boolean
        get() = errorCount >= 3
}