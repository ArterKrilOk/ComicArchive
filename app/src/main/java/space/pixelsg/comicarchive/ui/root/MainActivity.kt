package space.pixelsg.comicarchive.ui.root

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import space.pixelsg.comicarchive.ui.theme.ComicArchiveTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ComicArchiveTheme {
                MainScreen()
            }
        }
    }
}