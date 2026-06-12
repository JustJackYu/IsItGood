package com.juhyeonyu.isitgood.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Local cache of display preferences so they apply instantly at launch, before any network call.
// The backend remains the cross-device source of truth; this is synced from it when Settings loads/saves.
private val Context.uiDataStore by preferencesDataStore(name = "ui_prefs")

class UiPreferencesStore(private val context: Context) {
    companion object {
        private val FONT_SIZE_KEY = stringPreferencesKey("font_size")
        const val DEFAULT_FONT_SIZE = "MEDIUM"
    }

    val fontSizeFlow: Flow<String> =
        context.uiDataStore.data.map { it[FONT_SIZE_KEY] ?: DEFAULT_FONT_SIZE }

    suspend fun saveFontSize(fontSize: String) {
        context.uiDataStore.edit { it[FONT_SIZE_KEY] = fontSize }
    }
}
