package space.pixelsg.comicarchive.ui.reader.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemGesturesPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
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
import space.pixelsg.comicarchive.ui.components.CustomMotionDurationsScale
import space.pixelsg.comicarchive.ui.helper.teapot.features
import space.pixelsg.comicarchive.ui.reader.ReaderFeature
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(modifier: Modifier = Modifier, uri: String) {
    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        val feature = features(ReaderFeature::class)

        // Load initial uri
        LaunchedEffect(uri) {
            feature(ReaderFeature.Msg.Action.OpenUri(uri))
        }

        val state by feature.state.collectAsState()

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
        val pagerState = rememberPagerState(initialPage = 0) { state.pages.size }

        LaunchedEffect(pagerState.currentPage, pagerState.pageCount) {
            if (pagerState.pageCount > 0) {
                // Load current and next 5 pages
                feature(ReaderFeature.Msg.Action.LoadPages(pagerState.currentPage, count = 5))
            }
        }


        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            var isPageSliderVisible by remember { mutableStateOf(false) }
            val scope =
                rememberCoroutineScope { CustomMotionDurationsScale() + EmptyCoroutineContext }
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
                val pageState = state.pages[it]
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
                        pageState.isLoading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            strokeCap = StrokeCap.Round,
                        )

                        pageState.imagePath.isNullOrBlank().not() -> ZoomImage(
                            painter = rememberAsyncImagePainter(
                                pageState.imagePath,
                                filterQuality = FilterQuality.Low
                            ),
                            modifier = Modifier
                                .fillMaxSize(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            zoomState = zoomState,
                            onTap = { onPageTap() },
                        )

                        else -> Text(
                            text = "Error loading page\n${pageState.error}\n${pageState.errorCount}",
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }

            AnimatedVisibility(
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                visible = pagerState.pageCount > 0 && !isPageSliderVisible,
                modifier = Modifier
                    .padding(innerPadding)
                    .align(Alignment.BottomCenter),
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${pagerState.pageCount}",
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 100))
                        .clickable {
                            isPageSliderVisible = true
                            hideSlider()
                        }
                        .padding(horizontal = 12.dp),
                )
            }

            AnimatedVisibility(
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                visible = pagerState.pageCount > 0 && isPageSliderVisible,
                modifier = Modifier
                    .padding(innerPadding)
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