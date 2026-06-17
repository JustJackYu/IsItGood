package com.juhyeonyu.isitgood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.juhyeonyu.isitgood.data.local.UiPreferencesStore
import com.juhyeonyu.isitgood.data.model.UserPreferences
import com.juhyeonyu.isitgood.data.remote.RetrofitClient
import com.juhyeonyu.isitgood.utils.parseHttpError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class PreferencesLoadState {
    object Loading : PreferencesLoadState()
    object Loaded : PreferencesLoadState()
    data class Error(val message: String) : PreferencesLoadState()
}

sealed class PreferencesSaveState {
    object Idle : PreferencesSaveState()
    object Saving : PreferencesSaveState()
    object Saved : PreferencesSaveState()
    data class Error(val message: String) : PreferencesSaveState()
}

class SettingsViewModel(private val uiPreferencesStore: UiPreferencesStore) : ViewModel() {
    private val _loadState = MutableStateFlow<PreferencesLoadState>(PreferencesLoadState.Loading)
    val loadState: StateFlow<PreferencesLoadState> = _loadState.asStateFlow()

    // The working copy the UI binds to; edits mutate this before being saved.
    private val _prefs = MutableStateFlow(UserPreferences())
    val prefs: StateFlow<UserPreferences> = _prefs.asStateFlow()

    private val _saveState = MutableStateFlow<PreferencesSaveState>(PreferencesSaveState.Idle)
    val saveState: StateFlow<PreferencesSaveState> = _saveState.asStateFlow()

    // Last loaded/saved baseline; `isDirty` is true while the working copy differs from it.
    private var savedSnapshot = UserPreferences()
    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> = _isDirty.asStateFlow()

    private fun recomputeDirty() {
        _isDirty.value = _prefs.value != savedSnapshot
    }

    fun load() {
        viewModelScope.launch {
            _loadState.value = PreferencesLoadState.Loading
            try {
                _prefs.value = RetrofitClient.api.getPreferences()
                savedSnapshot = _prefs.value
                recomputeDirty()
                // Sync the local render cache so the whole app matches the stored font size.
                uiPreferencesStore.saveFontSize(_prefs.value.fontSize)
                _loadState.value = PreferencesLoadState.Loaded
            } catch (e: HttpException) {
                _loadState.value = PreferencesLoadState.Error(parseHttpError(e))
            } catch (e: Exception) {
                _loadState.value = PreferencesLoadState.Error("Something went wrong. Check your connection.")
            }
        }
    }

    fun setSummaryLength(value: String) = edit { it.copy(summaryLength = value) }
    fun setTone(value: String) = edit { it.copy(tone = value) }
    fun setMatureContent(allowed: Boolean) = edit { it.copy(allowMatureContent = allowed) }

    // Only updates the pending selection — the app-wide font size is committed in save().
    // The Settings screen previews this pending value via its own density override.
    fun setFontSize(value: String) = edit { it.copy(fontSize = value) }
    fun toggleLookOutFor(item: String) = edit {
        val current = it.lookOutFor
        it.copy(lookOutFor = if (item in current) current - item else current + item)
    }

    fun setDealDisplay(value: String) = edit { it.copy(dealDisplay = value) }
    fun setSaleAlertDiscount(value: Int?) = edit { it.copy(saleAlertDiscount = value) }
    fun setSaleAlertPrice(value: Double?) = edit { it.copy(saleAlertPrice = value) }
    fun setChatLeaveWarning(enabled: Boolean) = edit { it.copy(chatLeaveWarning = enabled) }

    // Applies a local edit and clears any stale "Saved"/"Error" status so the UI shows unsaved changes.
    private fun edit(transform: (UserPreferences) -> UserPreferences) {
        _prefs.value = transform(_prefs.value)
        recomputeDirty()
        if (_saveState.value !is PreferencesSaveState.Saving) {
            _saveState.value = PreferencesSaveState.Idle
        }
    }

    fun save() {
        viewModelScope.launch {
            _saveState.value = PreferencesSaveState.Saving
            try {
                _prefs.value = RetrofitClient.api.updatePreferences(_prefs.value)
                savedSnapshot = _prefs.value
                recomputeDirty()
                // Apply the new font size app-wide via the local render cache.
                uiPreferencesStore.saveFontSize(_prefs.value.fontSize)
                _saveState.value = PreferencesSaveState.Saved
            } catch (e: HttpException) {
                _saveState.value = PreferencesSaveState.Error(parseHttpError(e))
            } catch (e: Exception) {
                _saveState.value = PreferencesSaveState.Error("Couldn't save. Check your connection.")
            }
        }
    }
}

// Supplies the UiPreferencesStore dependency via Compose's viewModel(factory = ...).
class SettingsViewModelFactory(
    private val uiPreferencesStore: UiPreferencesStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SettingsViewModel(uiPreferencesStore) as T
}
