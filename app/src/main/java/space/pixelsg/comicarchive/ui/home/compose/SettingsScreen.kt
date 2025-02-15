package space.pixelsg.comicarchive.ui.home.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    gridSize: Float,
    onGridSizeChange: (Float) -> Unit,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Text("Grid size")
        Slider(
            value = gridSize,
            onValueChange = onGridSizeChange,
            steps = 4,
            valueRange = 50f..200f,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}