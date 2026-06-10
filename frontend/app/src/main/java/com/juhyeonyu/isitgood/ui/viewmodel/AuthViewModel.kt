package com.juhyeonyu.isitgood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juhyeonyu.isitgood.data.model.AuthRequest
import com.juhyeonyu.isitgood.data.remote.RetrofitClient
import com.juhyeonyu.isitgood.utils.parseHttpError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _state.value = AuthState.Error("Email cannot be empty")
            return
        }
        if (password.isBlank()) {
            _state.value = AuthState.Error("Password cannot be empty")
            return
        }
        if (password.length < 6) {
            _state.value = AuthState.Error("Password must be at least 6 characters long")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = RetrofitClient.api.login(AuthRequest(email, password))
                RetrofitClient.token = response.token
                _state.value = AuthState.Success(response.token)
            } catch (e: HttpException) {
                _state.value = AuthState.Error(parseHttpError(e))
            } catch (e: Exception) {
                _state.value = AuthState.Error("Something went wrong. Check your connection.")
            }
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank()) {
            _state.value = AuthState.Error("Email cannot be empty")
            return
        }
        if (password.isBlank()) {
            _state.value = AuthState.Error("Password cannot be empty")
            return
        }
        if (password.length < 6) {
            _state.value = AuthState.Error("Password must be at least 6 characters long")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = RetrofitClient.api.register(AuthRequest(email, password))
                RetrofitClient.token = response.token
                _state.value = AuthState.Success(response.token)
            } catch (e: HttpException) {
                _state.value = AuthState.Error(parseHttpError(e))
            } catch (e: Exception) {
                _state.value = AuthState.Error("Something went wrong. Check your connection.")
            }
        }
    }
}