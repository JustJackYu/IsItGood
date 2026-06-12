package com.juhyeonyu.isitgood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juhyeonyu.isitgood.data.model.BestDeal
import com.juhyeonyu.isitgood.data.model.DealGame
import com.juhyeonyu.isitgood.data.model.SavedGame
import com.juhyeonyu.isitgood.data.model.UserPreferences
import com.juhyeonyu.isitgood.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SavedGameState {
    object Idle : SavedGameState()
    object Loading : SavedGameState()
    data class Success(val games: List<SavedGame>) : SavedGameState()
    data class Error(val message: String) : SavedGameState()
}

sealed class DealsState {
    object Loading : DealsState()
    data class Success(val deals: List<DealGame>) : DealsState()
    data class Error(val message: String) : DealsState()
}

class HomeViewModel : ViewModel() {
    private val _savedGameState = MutableStateFlow<SavedGameState>(SavedGameState.Idle)
    val savedGameState: StateFlow<SavedGameState> = _savedGameState.asStateFlow()

    private val _dealsState = MutableStateFlow<DealsState>(DealsState.Loading)
    val dealsState: StateFlow<DealsState> = _dealsState.asStateFlow()

    // Current best deal per saved game (rawgId -> deal), for threshold highlighting.
    private val _savedDeals = MutableStateFlow<Map<Int, BestDeal>>(emptyMap())
    val savedDeals: StateFlow<Map<Int, BestDeal>> = _savedDeals.asStateFlow()

    // Drives dealDisplay rendering + sale-alert thresholds; defaults until loaded.
    private val _preferences = MutableStateFlow(UserPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    // Username for the personalized greeting; null until loaded (or if unset).
    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    fun load() {
        loadSavedGames()
        loadDeals()
        loadSavedDeals()
        loadPreferences()
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                _username.value = RetrofitClient.api.getMe().username
            } catch (e: Exception) {
                // Non-critical: greeting falls back to no name.
            }
        }
    }

    fun loadSavedGames() {
        viewModelScope.launch {
            _savedGameState.value = SavedGameState.Loading
            try {
                val games = RetrofitClient.api.getSavedGames()
                _savedGameState.value = SavedGameState.Success(games)
            } catch (e: Exception) {
                _savedGameState.value = SavedGameState.Error(e.message ?: "Failed to load saved games")
            }
        }
    }

    private fun loadDeals() {
        viewModelScope.launch {
            _dealsState.value = DealsState.Loading
            try {
                _dealsState.value = DealsState.Success(RetrofitClient.api.getDeals())
            } catch (e: Exception) {
                _dealsState.value = DealsState.Error(e.message ?: "Failed to load deals")
            }
        }
    }

    private fun loadSavedDeals() {
        viewModelScope.launch {
            try {
                val list = RetrofitClient.api.getSavedGameDeals()
                _savedDeals.value = list.mapNotNull { sd -> sd.deal?.let { sd.rawgId to it } }.toMap()
            } catch (e: Exception) {
                // Non-critical: highlights simply won't show.
            }
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            try {
                _preferences.value = RetrofitClient.api.getPreferences()
            } catch (e: Exception) {
                // Keep defaults.
            }
        }
    }
}
