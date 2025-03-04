package space.pixelsg.comicarchive.ui.reader.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemGesturesPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.rememberAsyncImagePainter
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import space.pixelsg.comicarchive.R
import space.pixelsg.comicarchive.ui.components.CustomMotionDurationsScale
import space.pixelsg.comicarchive.ui.navigation.Destination
import space.pixelsg.comicarchive.ui.reader.ReaderViewModel
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(modifier: Modifier = Modifier, destination: Destination.Reader) {
    Scaffold(
        modifier = modifier,
    ) { innerPadding ->

        val scope = rememberCoroutineScope()

        val viewModel = koinViewModel<ReaderViewModel> { parametersOf(destination) }

        val state by viewModel.screenState.collectAsState()
        val pages by viewModel.pages.collectAsState()

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            if (state.isLoading) CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                strokeCap = StrokeCap.Round,
            )
        }

        var userScrollEnabled by remember { mutableStateOf(true) }
        var isPageSliderVisible by remember { mutableStateOf(false) }
        val pagerState = rememberPagerState(initialPage = 0) { pages.size }

        LaunchedEffect(pagerState.currentPage, pagerState.pageCount) {
            if (pagerState.pageCount > 0) {
                // Load current and next 5 pages
                viewModel.loadPages(
                    pagerState.currentPage,
                    count = if (isPageSliderVisible) 0 else 5, // Do NOT preload multiple items then slider is active to avoid massive preloads during fast scroll
                )
            }
        }

        var showPageSelectorDialog by remember { mutableStateOf(false) }
        PageSelectorDialog(
            show = showPageSelectorDialog,
            currentPage = pagerState.currentPage + 1,
            range = 0..<pagerState.pageCount,
            onDismiss = { showPageSelectorDialog = false },
            onApply = { scope.launch { pagerState.scrollToPage(it + 1) } },
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            var hideSliderJob: Job? by remember { mutableStateOf(null) }

            fun hideSlider(instantly: Boolean = false) {
                hideSliderJob?.cancel()
                hideSliderJob = scope.launch {
                    if (!instantly) delay(4.seconds)
                    isPageSliderVisible = false
                }
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = userScrollEnabled,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 6.dp,
            ) {
                val pageState = pages[it]
                val isActivePage = it == pagerState.currentPage

                val zoomState = rememberZoomState()
                var isDefaultZoom by remember { mutableStateOf(true) }

                LaunchedEffect(zoomState.zoomable) {
                    zoomState.zoomable.threeStepScale = false
                }

                // Enable subsampling for large pages
                LaunchedEffect(pageState.imagePath, zoomState.subsampling) {
                    if (pageState.imagePath.isNullOrBlank()) return@LaunchedEffect
                    zoomState.setSubsamplingImage(ImageSource.fromFile(pageState.imagePath))
                }
                // Check is zoomed
                LaunchedEffect(zoomState.zoomable.transform.scale) {
                    isDefaultZoom =
                        zoomState.zoomable.transform.scale.scaleX <= zoomState.zoomable.baseTransform.scale.scaleX
                                || zoomState.zoomable.transform.scale.scaleY <= zoomState.zoomable.baseTransform.scale.scaleY
                }
                // Restore default zoom when page is not active
                LaunchedEffect(isActivePage) {
                    if (!isActivePage) {
                        withContext(CustomMotionDurationsScale()) {
                            zoomState.zoomable.reset("reset_zoom_then_nonactive")
                        }
                    }
                }
                // Lock pager scrolls then zoomed
                LaunchedEffect(isDefaultZoom) {
                    if (isActivePage) userScrollEnabled = isDefaultZoom
                }

                fun onPageTap() {
                    if (isPageSliderVisible) hideSlider(instantly = true)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        pageState.isRetryVisible -> Button(
                            onClick = {
                                viewModel.retryPage(pageState.pagePath)
                            }
                        ) {
                            Text(text = stringResource(R.string.retry))
                        }

                        pageState.isLoading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            strokeCap = StrokeCap.Round,
                        )

                        pageState.imagePath.isNullOrBlank().not() -> ZoomImage(
                            painter = rememberAsyncImagePainter(
                                pageState.imagePath,
                                filterQuality = FilterQuality.Low,
                                onError = { error ->
                                    viewModel.pageFailed(pageState.pagePath, error.result.throwable)
                                },
                            ),
                            modifier = Modifier
                                .fillMaxSize(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            zoomState = zoomState,
                            onTap = { onPageTap() },
                        )

                        pageState.error.isNullOrBlank().not() -> Text(
                            text = "Error loading page\n${pageState.error}\n${pageState.errorCount}",
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }

            AnimatedVisibility(
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                visible = pagerState.pageCount > 0 && !isPageSliderVisible,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(bottom = 12.dp)
                    .align(Alignment.BottomCenter),
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${pagerState.pageCount}",
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 100))
                        .combinedClickable(
                            onLongClick = { showPageSelectorDialog = true },
                            onClick = {
                                isPageSliderVisible = true
                                hideSlider()
                            },
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }

            AnimatedVisibility(
                visible = isPageSliderVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .padding(innerPadding)
                    .align(Alignment.TopCenter),
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${pagerState.pageCount}",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                visible = pagerState.pageCount > 0 && isPageSliderVisible,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .systemGesturesPadding()
                    .align(Alignment.BottomCenter),
            ) {
                val pagesCount = pagerState.pageCount - 1

                Slider(
                    steps = pagesCount - 2,
                    modifier = Modifier.fillMaxWidth(),
                    value = pagerState.currentPage.toFloat(),
                    valueRange = 0f..pagesCount.toFloat(),
                    onValueChange = {
                        scope.launch {
                            hideSliderJob?.cancelAndJoin()
                            pagerState.scrollToPage(it.toInt())
                        }
                    },
                    onValueChangeFinished = { hideSlider() }
                )
            }
        }
    }
}

@Composable
private fun PageSelectorDialog(
    show: Boolean,
    currentPage: Int,
    range: IntRange,
    onDismiss: () -> Unit,
    onApply: (Int) -> Unit,
) {
    if (show) Dialog(
        onDismissRequest = onDismiss,
    ) {
        var value by remember { mutableStateOf(currentPage.toString()) }

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.page_selector_dialog_title),
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = value,
                shape = RoundedCornerShape(12.dp),
                onValueChange = { newValue ->
                    value = newValue.filter { it.isDigit() }
                    value.toIntOrNull()?.let { intValue ->
                        value = intValue.coerceIn(range).toString()
                    }
                },
                label = { Text(stringResource(R.string.page_selector_hind)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    onApply(value.toIntOrNull() ?: 0)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.apply))
            }
        }
    }
}