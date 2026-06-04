package com.juhyeonyu.isitgood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juhyeonyu.isitgood.data.model.Game
import com.juhyeonyu.isitgood.data.model.SavedGame
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

class HomeViewModel : ViewModel() {
    private val _savedGameState = MutableStateFlow<SavedGameState>(SavedGameState.Idle)
    val savedGameState: StateFlow<SavedGameState> = _savedGameState.asStateFlow()


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
}