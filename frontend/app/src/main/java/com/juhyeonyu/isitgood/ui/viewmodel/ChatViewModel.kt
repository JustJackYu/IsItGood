package com.juhyeonyu.isitgood.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juhyeonyu.isitgood.data.model.ChatMessage
import com.juhyeonyu.isitgood.data.model.ChatRequest
import com.juhyeonyu.isitgood.data.remote.RetrofitClient
import com.juhyeonyu.isitgood.data.repository.GameRepository
import com.juhyeonyu.isitgood.utils.parseHttpError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class ChatState {
    object Idle : ChatState()
    object Loading : ChatState()
    data class Error(val message: String) : ChatState()
}

class ChatViewModel : ViewModel() {
    val messages = mutableStateListOf<Pair<String, String>>()
    var gameTitle: String = ""
    var summary: String = ""

    private val _state = MutableStateFlow<ChatState>(ChatState.Idle)
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun loadGameContext(rawgId: Int) {
        gameTitle = GameRepository.getGame(rawgId)?.name ?: ""
        summary = GameRepository.getSummary(rawgId) ?: ""
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            messages.add("user" to userMessage)
            _state.value = ChatState.Loading
            try {
                val history = messages.dropLast(1).map { (role, content) ->
                    ChatMessage(role, content)
                }
                val response = RetrofitClient.api.chat(
                    ChatRequest(userMessage, gameTitle, summary, history)
                )
                messages.add("assistant" to response.reply)
                _state.value = ChatState.Idle
            } catch (e: HttpException) {
                _state.value = ChatState.Error(parseHttpError(e))
            } catch (e: Exception) {
                _state.value = ChatState.Error("Something went wrong. Check your connection.")
            }
        }
    }
}