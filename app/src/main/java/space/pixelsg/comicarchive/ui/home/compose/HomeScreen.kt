package space.pixelsg.comicarchive.ui.home.compose

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.InfiniteTransition
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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import org.koin.compose.viewmodel.koinViewModel
import space.pixelsg.comicarchive.R
import space.pixelsg.comicarchive.ui.components.AnimatedArrowPointer
import space.pixelsg.comicarchive.ui.components.FlowCollectEffect
import space.pixelsg.comicarchive.ui.components.UiPreferences
import space.pixelsg.comicarchive.ui.components.rememberUiPref
import space.pixelsg.comicarchive.ui.home.ComicState
import space.pixelsg.comicarchive.ui.home.HomeSnackBar
import space.pixelsg.comicarchive.ui.home.HomeViewModel
import space.pixelsg.comicarchive.ui.navigation.Destination
import space.pixelsg.comicarchive.ui.root.LocalDrawerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    navController: NavController,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // App bar behaviour
    var canScrollAppBar by remember { mutableStateOf(true) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        canScroll = { canScrollAppBar },
    )

    // ViewModel and state
    val viewModel: HomeViewModel = koinViewModel()
    val items by viewModel.items.collectAsState()
    val isEditMode by viewModel.isEditMode

    // Local context
    val context = LocalContext.current

    // Collect snack bar effects
    FlowCollectEffect(viewModel.snacksFlow, latest = true) { snack ->
        when (snack) {
            is HomeSnackBar.ComicAddFailed -> snackbarHostState.showSnackbar(
                message = context.getString(R.string.failed_to_add_comics, snack.name)
            )

            is HomeSnackBar.ComicAdded -> snackbarHostState.showSnackbar(
                message = context.getString(R.string.comic_added, snack.name)
            )

            is HomeSnackBar.ComicRemoved -> snackbarHostState.showSnackbar(
                message = context.getString(R.string.comic_removed, snack.name),
                actionLabel = context.getString(R.string.comic_removed_restore),
                duration = SnackbarDuration.Long,
            ).let { result ->
                if (result == SnackbarResult.ActionPerformed) snack.action()
            }
        }
    }

    // File dialog launcher
    val fileSelectorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.map { uri ->
            // Request permanent permission to access file
            val contentResolver = context.applicationContext.contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
            uri.toString()
        }.let { viewModel.addUris(it) }
    }

    // Transition for infinite animations
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    LaunchedEffect(items) {
        canScrollAppBar = items.isNotEmpty()
    }

    // Drawer state
    val drawerState = LocalDrawerState.current

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.comic_archive)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = { scope.launch { drawerState?.open() } },
                    ) {
                        Icon(Icons.Rounded.Menu, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
            // Fab pulsating animation
            val addFabAnimatedScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (items.isEmpty()) 1.15f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "add_fab_scale",
            )

            FloatingActionButton(
                modifier = Modifier.scale(addFabAnimatedScale),
                onClick = {
                    Firebase.analytics.logEvent("comic_add") {
                        param("source", "home_screen")
                        param("type", "local")
                    }
                    // Open file selector
                    fileSelectorLauncher.launch(
                        arrayOf(
                            "application/zip",
                            "application/pdf",
                            "video/x-mng",
                        )
                    )
                },
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
            }
        },
    ) { paddingInsets ->
        if (items.isEmpty()) Column(
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
                viewModel.moveItems(fromComicId, toComicId)
            }
        )

        val gridSize by rememberUiPref(
            context = LocalContext.current,
            key = UiPreferences.GRID_SCALE,
            defaultValue = 150f,
        )

        val firstVisibleItemIndex by remember { derivedStateOf { gridState.firstVisibleItemIndex } }
        val visibleItemsCount by remember { derivedStateOf { gridState.layoutInfo.visibleItemsInfo.size } }

        LaunchedEffect(items.size, firstVisibleItemIndex, visibleItemsCount) {
            val itemsCount = (visibleItemsCount * 2f).toInt()
            val first = firstVisibleItemIndex - 2 // Make sure to preload a little bit before
            viewModel.loadPosters(first, itemsCount)
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
            items(
                items = items,
                key = { it.comic.id },
            ) {
                ReorderableItem(
                    state = reorderableState,
                    orientationLocked = false,
                    key = it.comic.id,
                    defaultDraggingModifier = Modifier,
                ) { isDragging ->
                    CardItem(
                        state = it,
                        reorderableState = reorderableState,
                        isDragging = isDragging,
                        isInEditMode = isEditMode,
                        itemScale = itemScale,
                        itemRotation = itemRotation,
                        infiniteTransition = infiniteTransition,
                        onExitEdit = viewModel::exitEditMode,
                        onRemove = viewModel::removeComic,
                        onClick = { comicState ->
                            navController.navigate(Destination.Reader(uri = comicState.comic.uri))
                        },
                        onLongClick = viewModel::enterEditMode,
                    )
                }
            }

            if (items.size > visibleItemsCount) item(
                key = "items_counter",
                contentType = "footer",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text = stringResource(R.string.comic_counter, items.size),
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(paddingInsets)
                .fillMaxSize()
        ) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                Snackbar(
                    snackbarData = it,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoxScope.CardItem(
    isDragging: Boolean = false,
    isInEditMode: Boolean = false,
    itemScale: Float = 1f,
    itemRotation: Float = 0f,
    infiniteTransition: InfiniteTransition,
    reorderableState: ReorderableState<*>,
    onRemove: (ComicState) -> Unit,
    onClick: (ComicState) -> Unit,
    onLongClick: () -> Unit,
    onExitEdit: () -> Unit,
    state: ComicState,
) {
    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

    Column(
        modifier = Modifier
            .scale(itemScale)
            .rotate(if (isInEditMode) itemRotation else 0f)
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
                    if (isInEditMode) detectReorderAfterLongPress(reorderableState)
                        .clickable { onExitEdit() }
                    else combinedClickable(
                        onLongClickLabel = stringResource(R.string.edit),
                        onLongClick = { onLongClick() },
                        onClick = { onClick(state) },
                    )
                }
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isLoading) {
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
            if (!state.error.isNullOrBlank()) Text(state.error)
            if (state.imagePath != null) AsyncImage(
                model = state.imagePath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Text(
            text = state.comic.name,
            minLines = 2,
            maxLines = 2,
            fontSize = 14.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    AnimatedVisibility(
        visible = isInEditMode && !isDragging,
        modifier = Modifier.align(Alignment.TopEnd),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer)
                .clickable {
                    onRemove(state)
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