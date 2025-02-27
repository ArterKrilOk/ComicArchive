package space.pixelsg.comicarchive.ui.home.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import space.pixelsg.comicarchive.R
import space.pixelsg.comicarchive.ui.components.AnimatedArrowPointer
import space.pixelsg.comicarchive.ui.components.UiPreferences
import space.pixelsg.comicarchive.ui.components.rememberUiPref
import space.pixelsg.comicarchive.ui.helper.teapot.features
import space.pixelsg.comicarchive.ui.home.HomeFeature

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    var canScrollAppBar by remember { mutableStateOf(true) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        canScroll = { canScrollAppBar },
    )

    val feature = features(HomeFeature::class)

    LaunchedEffect(currentCompositeKeyHash) {
        feature(HomeFeature.Msg.Action.Init)
    }

    val state by feature.state.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }

    LaunchedEffect(state.items) {
        canScrollAppBar = state.items.isNotEmpty()
        if (isEditMode && state.items.isEmpty()) isEditMode = false
    }

    val addFabAnimatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state.items.isEmpty()) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "add_fab_scale",
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.comic_archive)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = {
                            feature(HomeFeature.Msg.Action.OpenDrawer)
                        },
                    ) {
                        Icon(Icons.Rounded.Menu, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.scale(addFabAnimatedScale),
                onClick = {
                    Firebase.analytics.logEvent("comic_add") {
                        param("source", "home_screen")
                        param("type", "local")
                    }
                    feature(
                        HomeFeature.Msg.Action.OpenFileDialog(
                            listOf(
                                "application/zip",
                                "application/pdf",
                                "video/x-mng",
                            )
                        )
                    )
                },
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
            }
        },
    ) { paddingInsets ->
        if (state.items.isEmpty()) Column(
            modifier = Modifier
                .padding(paddingInsets)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(.7f))
            Text(
                modifier = Modifier,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                text = stringResource(R.string.there_is_nothing_here_yet),
            )
            AnimatedArrowPointer(
                dashLength = 6.dp,
                strokeWidth = 3.dp,
                pointerSize = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .padding(48.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }


        fun exitEditMode() {
            isEditMode = false
            feature(HomeFeature.Msg.Action.ApplyEditedPositions)
        }

        val itemScale by animateFloatAsState(
            targetValue = if (isEditMode) .9f else 1f,
        )

        val itemRotation by infiniteTransition.animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(150, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        )

        val gridState = rememberLazyGridState()
        val reorderableState = rememberReorderableLazyGridState(
            gridState = gridState,
            onMove = { from, to ->
                val fromComicId = from.key as Long
                val toComicId = to.key as Long
                feature(HomeFeature.Msg.Action.Move(fromComicId, toComicId))
            }
        )

        val firstVisibleItemIndex by remember { derivedStateOf { gridState.firstVisibleItemIndex } }
        var gridSize by rememberUiPref(
            LocalContext.current,
            UiPreferences.GRID_SCALE,
            defaultValue = 150f,
        )

        LaunchedEffect(firstVisibleItemIndex, state.items.size, gridSize) {
            val itemsCount =
                (gridState.layoutInfo.visibleItemsInfo.size * 2f).toInt()
            val first = firstVisibleItemIndex - 2 // Make sure to preload a little bit before

            feature(HomeFeature.Msg.Action.LoadPosters(first, itemsCount))
        }

        LazyVerticalGrid(
            modifier = Modifier
                .padding(paddingInsets)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .then(Modifier.reorderable(reorderableState)),
            state = gridState,
            columns = GridCells.Adaptive(gridSize.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.importState != null) item(
                key = "processing",
                contentType = "ui",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable(state.importState?.processing?.size == 0) {
                            feature(HomeFeature.Msg.Action.ClearImportState)
                        }
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val progress by animateFloatAsState(
                        targetValue = state.importState?.progress ?: 1f,
                        animationSpec = tween(durationMillis = 1000),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Importing",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        val itemsCount = state.importState?.itemsCount ?: 0
                        val processedCount = state.importState?.completed?.size ?: 0

                        Text(
                            text = "$processedCount/$itemsCount",
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            items(
                items = state.items,
                key = { it.comic.id },
            ) {
                ReorderableItem(
                    state = reorderableState,
                    orientationLocked = false,
                    key = it.comic.id,
                    defaultDraggingModifier = Modifier,
                ) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                    Column(
                        modifier = Modifier
                            .scale(itemScale)
                            .rotate(if (isEditMode) itemRotation else 0f)
                            .scale(if (isDragging) 1.1f else 1f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(
                            6.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .aspectRatio(.7f)
                                .shadow(elevation)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .run {
                                    if (isEditMode) detectReorderAfterLongPress(reorderableState)
                                        .clickable { exitEditMode() }
                                    else combinedClickable(
                                        onLongClickLabel = stringResource(R.string.edit),
                                        onLongClick = { isEditMode = true },
                                        onClick = {
                                            feature(HomeFeature.Msg.Action.PreviewComic(it.comic.uri))
                                        }
                                    )
                                }
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (it.isLoading) {
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = .2f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "alpha_inf"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(alpha)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                )
                            }
                            if (!it.error.isNullOrBlank()) Text(it.error)
                            if (it.imagePath != null) AsyncImage(
                                model = it.imagePath,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        Text(
                            text = it.comic.name,
                            minLines = 2,
                            maxLines = 2,
                            fontSize = 14.sp,
                            lineHeight = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    AnimatedVisibility(
                        visible = isEditMode && !isDragging,
                        modifier = Modifier.align(Alignment.TopEnd),
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .clickable {
                                    feature(HomeFeature.Msg.Action.RemoveComic(it.comic.id))
                                }
                                .padding(6.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}