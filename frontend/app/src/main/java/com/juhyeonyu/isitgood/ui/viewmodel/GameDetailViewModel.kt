package com.juhyeonyu.isitgood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juhyeonyu.isitgood.data.model.Deal
import com.juhyeonyu.isitgood.data.model.Game
import com.juhyeonyu.isitgood.data.model.SaveGameRequest
import com.juhyeonyu.isitgood.data.remote.RetrofitClient
import com.juhyeonyu.isitgood.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GameState {
    object Idle : GameState()
    data class Success(val game: Game) : GameState()
    object NotFound : GameState()
}

sealed class SummaryState {
    object Idle : SummaryState()
    object Loading : SummaryState()
    data class Success(val summary: String, val sources: List<String>) : SummaryState()
    data class Error(val message: String) : SummaryState()
}

sealed class PricesState {
    object Idle : PricesState()
    object Loading : PricesState()
    data class Success(val prices: List<Deal>) : PricesState()
    data class Error(val message: String) : PricesState()
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Saved : SaveState()
    object SavedPermanent : SaveState()
    object Unsaving : SaveState()
    object Unsaved : SaveState()
    data class Error(val message: String) : SaveState()
}

class GameDetailViewModel : ViewModel() {
    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _summaryState = MutableStateFlow<SummaryState>(SummaryState.Idle)
    val summaryState: StateFlow<SummaryState> = _summaryState.asStateFlow()

    private val _pricesState = MutableStateFlow<PricesState>(PricesState.Idle)
    val pricesState: StateFlow<PricesState> = _pricesState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun loadGame(rawgId: Int) {
        viewModelScope.launch {
            val game = GameRepository.getOrFetchGame(rawgId)
            _gameState.value = if (game != null) GameState.Success(game) else GameState.NotFound
        }
    }

    fun checkIfSaved(rawgId: Int) {
        viewModelScope.launch {
            try {
                val savedGames = RetrofitClient.api.getSavedGames()
                val alreadySaved = savedGames.any { it.rawgId == rawgId }
                _saveState.value = if (alreadySaved) SaveState.SavedPermanent else SaveState.Idle
            } catch (e: Exception) {
                _saveState.value = SaveState.Idle
            }
        }
    }

    fun loadSummary(id: Int, name: String) {
        viewModelScope.launch {
            _summaryState.value = SummaryState.Loading
            try {
                val summary = RetrofitClient.api.getGameSummary(id, name)
                GameRepository.cacheSummary(id, summary.summary)
                _summaryState.value = SummaryState.Success(summary.summary, summary.sources)
            } catch (e: Exception) {
                _summaryState.value = SummaryState.Error(e.message ?: "Failed to load summary")
            }
        }
    }

    fun loadPrices(id: Int, name: String) {
        viewModelScope.launch {
            _pricesState.value = PricesState.Loading
            try {
                val prices = RetrofitClient.api.getGamePrices(id, name)
                _pricesState.value = PricesState.Success(prices)
            } catch (e: Exception) {
                _pricesState.value = PricesState.Error(e.message ?: "Failed to load prices")
            }
        }
    }

    fun saveGame(
        id: Int,
        name: String,
        coverImage: String? = null,
        rating: Float? = null,
        released: String? = null
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                RetrofitClient.api.saveGame(SaveGameRequest(id, name, coverImage, rating, released))
                _saveState.value = SaveState.Saved
                delay(1500)
                _saveState.value = SaveState.SavedPermanent
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to save game")
            }
        }
    }

    fun unsaveGame(id: Int) {
        viewModelScope.launch {
            _saveState.value = SaveState.Unsaving
            try {
                RetrofitClient.api.unsaveGame(id)
                _saveState.value = SaveState.Unsaved
                delay(1500)
                _saveState.value = SaveState.Idle
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to unsave game")
            }
        }
    }
}