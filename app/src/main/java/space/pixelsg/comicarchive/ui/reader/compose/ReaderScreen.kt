package space.pixelsg.comicarchive.ui.reader.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import space.pixelsg.comicarchive.ui.helper.teapot.features
import space.pixelsg.comicarchive.ui.reader.ReaderFeature
import kotlin.math.max

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
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = userScrollEnabled,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 6.dp,
            ) {
                val pageState = state.pages[it]
                val isActivePage = it == pagerState.currentPage

                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                val animatedScale by animateFloatAsState(
                    targetValue = scale,
                    label = "scale_animation",
                    animationSpec = spring(dampingRatio = .7f),
                )
                val animatedOffset by animateOffsetAsState(
                    targetValue = offset,
                    label = "offset_animation",
                    animationSpec = spring(dampingRatio = .7f),
                )

                val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                    scale = max(1f, scale * zoomChange)
                    offset = if (scale == 1f) Offset.Zero else offset + (offsetChange / scale)
                }

                // Reset zoom values
                fun resetZoom() {
                    scale = 1f
                    offset = Offset.Zero
                }

                // Restore default zoom when page is not active
                LaunchedEffect(isActivePage) {
                    if (!isActivePage) resetZoom()
                }
                // Lock pager scrolls then zoomed
                LaunchedEffect(scale) {
                    if (isActivePage) userScrollEnabled = scale == 1f
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        pageState.isLoading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            strokeCap = StrokeCap.Round,
                        )

                        pageState.imagePath.isNullOrBlank().not() -> Box(
                            modifier = Modifier
                                .transformable(
                                    state = transformState,
                                    canPan = { scale != 1f },
                                )
                                .combinedClickable(
                                    interactionSource = null,
                                    indication = null,
                                    onDoubleClick = {
                                        if (scale == 1f && offset == Offset.Zero) scale = 2.5f
                                        else resetZoom()
                                    },
                                    onClick = { },
                                )
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .graphicsLayer(
                                        scaleX = animatedScale,
                                        scaleY = animatedScale,
                                        translationX = animatedOffset.x,
                                        translationY = animatedOffset.y
                                    )
                                    .fillMaxSize(),
                                model = pageState.imagePath,
                                contentDescription = null,
                                filterQuality = FilterQuality.High,
                            )
                        }

                        else -> Text(
                            text = "Error loading page\n${pageState.error}\n${pageState.errorCount}",
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }

            if (pagerState.pageCount > 0) Text(
                text = "${pagerState.currentPage + 1}/${pagerState.pageCount}",
                modifier = Modifier
                    .padding(innerPadding)
                    .align(Alignment.BottomCenter),
            )
        }
    }
}