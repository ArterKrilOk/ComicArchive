package space.pixelsg.comicarchive.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.uiPreferencesDataStore by preferencesDataStore(name = "ui_prefs")

object UiPreferences {
    val GRID_SCALE = floatPreferencesKey("grid_scale")
}

@Composable
fun <T> rememberUiPref(
    context: Context,
    key: Preferences.Key<T>,
    defaultValue: T
): MutableState<T> {
    val dataStore = context.uiPreferencesDataStore

    val valueState = remember { mutableStateOf(defaultValue) }
    var initialLoadCompleted by remember { mutableStateOf(false) }

    // Read initial value from DataStore
    LaunchedEffect(key) {
        dataStore.data.map { it[key] ?: defaultValue }
            .collect {
                valueState.value = it
                initialLoadCompleted = true
            }
    }

    // Auto-save changes to DataStore
    LaunchedEffect(valueState.value, initialLoadCompleted) {
        if (initialLoadCompleted) {
            val prefs = dataStore.data.firstOrNull()
            if (prefs?.get(key) != valueState) dataStore.edit {
                it[key] = valueState.value
            }
        }
    }

    return valueState
}
