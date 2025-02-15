package space.pixelsg.comicarchive.ui.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import space.pixelsg.comicarchive.ui.helper.teapot.features

@Composable
fun TestScreen(
    modifier: Modifier,
) {
    val feature = features(TestFeature::class)

    Scaffold(modifier = modifier) { paddingInsets ->
        Column(
            modifier = Modifier.padding(paddingInsets),
        ) {
            val state by feature.state.collectAsState()

            Text(currentCompositeKeyHash.toString())

            Text(state.counter.toString())

            Button(onClick = { feature(TestFeature.Msg.Action.Increment) }) {
                Text("Increment")
            }

            Button(
                onClick = { feature(TestFeature.Msg.Action.NavigateHome) },
            ) {
                Text("Navigate Up")
            }
        }
    }
}