package space.pixelsg.comicarchive.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import space.pixelsg.comicarchive.R
import space.pixelsg.comicarchive.ui.components.UiPreferences
import space.pixelsg.comicarchive.ui.components.rememberUiPref
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    Text(stringResource(R.string.settings))
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GridSizeSetting()
        }
    }
}

private val GridSizes = listOf(
    50f, 60f, 75f, 100f, 150f, 200f
)

@Composable
private fun GridSizeSetting(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.grid_size),
        style = MaterialTheme.typography.titleLarge,
    )
    var gridSize by rememberUiPref(
        LocalContext.current,
        UiPreferences.GRID_SCALE,
        defaultValue = 150f,
    )

    val gridState = rememberLazyGridState()

    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 8.dp,
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 12.dp)
                .padding(horizontal = 12.dp),
            state = gridState,
            userScrollEnabled = false,
            columns = GridCells.Adaptive(gridSize.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(count = 24) {
                Box(
                    modifier = Modifier
                        .aspectRatio(.7f)
                        .alpha(.7f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                )
            }
        }
    }

    Slider(
        value = GridSizes.indexOf(gridSize).toFloat(),
        onValueChange = {
            val index = it.roundToInt()
            gridSize = GridSizes[index]
        },
        steps = GridSizes.size - 2,
        valueRange = 0f..(GridSizes.size - 1).toFloat(),
        modifier = Modifier.fillMaxWidth(),
    )
}