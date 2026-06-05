package com.juhyeonyu.isitgood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juhyeonyu.isitgood.data.model.Deal
import com.juhyeonyu.isitgood.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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


class GameDetailViewModel : ViewModel() {
    private val _summaryState = MutableStateFlow<SummaryState>(SummaryState.Idle)
    val summaryState: StateFlow<SummaryState> = _summaryState.asStateFlow()

    private val _pricesState = MutableStateFlow<PricesState>(PricesState.Idle)
    val pricesState: StateFlow<PricesState> = _pricesState.asStateFlow()

    fun loadSummary(id: Int, name: String) {
        viewModelScope.launch {
            _summaryState.value = SummaryState.Loading
            try {
                val summary = RetrofitClient.api.getGameSummary(id, name)
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
}