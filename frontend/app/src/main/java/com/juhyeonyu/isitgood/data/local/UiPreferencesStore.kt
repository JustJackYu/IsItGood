package com.juhyeonyu.isitgood.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Local cache of display preferences so they apply instantly at launch, before any network call.
// The backend remains the cross-device source of truth; this is synced from it when Settings loads/saves.
private val Context.uiDataStore by preferencesDataStore(name = "ui_prefs")

class UiPreferencesStore(private val context: Context) {
    companion object {
        private val FONT_SIZE_KEY = stringPreferencesKey("font_size")
        const val DEFAULT_FONT_SIZE = "MEDIUM"
    }

    // In-memory source of truth for the theme. Updated synchronously so a font-size change
    // recomposes the theme instantly, rather than waiting on a DataStore round-trip.
    private val _fontSize = MutableStateFlow(DEFAULT_FONT_SIZE)
    val fontSize: StateFlow<String> = _fontSize.asStateFlow()

    // Seeds the in-memory value from disk; call once at startup.
    suspend fun loadFontSize() {
        _fontSize.value = context.uiDataStore.data
            .map { it[FONT_SIZE_KEY] ?: DEFAULT_FONT_SIZE }
            .first()
    }

    suspend fun saveFontSize(fontSize: String) {
        _fontSize.value = fontSize                                   // instant for the theme
        context.uiDataStore.edit { it[FONT_SIZE_KEY] = fontSize }    // persist for next launch
    }
}
