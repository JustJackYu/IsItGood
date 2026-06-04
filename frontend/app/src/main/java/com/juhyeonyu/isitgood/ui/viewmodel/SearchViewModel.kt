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

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val results: List<Game>) : SearchState()
    data class Error(val message: String) : SearchState()
}

class SearchViewModel : ViewModel() {
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    fun searchGames(query: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val results = RetrofitClient.api.searchGames(query)
                _searchState.value = SearchState.Success(results)
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Failed to search games")
            }
        }
    }
}