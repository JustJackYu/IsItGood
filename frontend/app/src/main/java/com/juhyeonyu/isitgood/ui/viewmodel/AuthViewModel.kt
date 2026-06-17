package com.juhyeonyu.isitgood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.juhyeonyu.isitgood.data.local.TokenStore
import com.juhyeonyu.isitgood.data.model.AuthRequest
import com.juhyeonyu.isitgood.data.model.ChangePasswordRequest
import com.juhyeonyu.isitgood.data.model.RegisterRequest
import com.juhyeonyu.isitgood.data.model.UpdateUsernameRequest
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

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

sealed class UsernameState {
    object Idle : UsernameState()
    object Loading : UsernameState()
    object Success : UsernameState()
    data class Error(val message: String) : UsernameState()
}

sealed class DeleteAccountState {
    object Idle : DeleteAccountState()
    object Loading : DeleteAccountState()
    object Success : DeleteAccountState()
    data class Error(val message: String) : DeleteAccountState()
}

class AuthViewModel(private val tokenStore: TokenStore) : ViewModel() {
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
                tokenStore.saveToken(response.token)
                _state.value = AuthState.Success(response.token)
            } catch (e: HttpException) {
                _state.value = AuthState.Error(parseHttpError(e))
            } catch (e: Exception) {
                _state.value = AuthState.Error("Something went wrong. Check your connection.")
            }
        }
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            tokenStore.clearToken()
            RetrofitClient.token = null
            _state.value = AuthState.Idle
        }
    }

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String) {
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            _changePasswordState.value = ChangePasswordState.Error("Both fields are required")
            return
        }

        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading
            try {
                RetrofitClient.api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
                _changePasswordState.value = ChangePasswordState.Success
            } catch (e: HttpException) {
                _changePasswordState.value = ChangePasswordState.Error(parseHttpError(e))
            } catch (e: Exception) {
                _changePasswordState.value = ChangePasswordState.Error("Something went wrong. Check your connection.")
            }
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }

    // Current account username, for prefilling the Settings editor.
    private val _accountUsername = MutableStateFlow<String?>(null)
    val accountUsername: StateFlow<String?> = _accountUsername.asStateFlow()

    private val _usernameState = MutableStateFlow<UsernameState>(UsernameState.Idle)
    val usernameState: StateFlow<UsernameState> = _usernameState.asStateFlow()

    fun loadAccount() {
        viewModelScope.launch {
            try {
                _accountUsername.value = RetrofitClient.api.getMe().username
            } catch (e: Exception) {
                // Non-critical.
            }
        }
    }

    fun updateUsername(username: String) {
        if (username.isBlank()) {
            _usernameState.value = UsernameState.Error("Username cannot be empty")
            return
        }

        viewModelScope.launch {
            _usernameState.value = UsernameState.Loading
            try {
                val res = RetrofitClient.api.updateUsername(UpdateUsernameRequest(username.trim()))
                _accountUsername.value = res.username
                _usernameState.value = UsernameState.Success
            } catch (e: HttpException) {
                _usernameState.value = UsernameState.Error(parseHttpError(e))
            } catch (e: Exception) {
                _usernameState.value = UsernameState.Error("Couldn't update username. Check your connection.")
            }
        }
    }

    fun resetUsernameState() {
        _usernameState.value = UsernameState.Idle
    }

    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState: StateFlow<DeleteAccountState> = _deleteAccountState.asStateFlow()

    fun deleteAccount(confirmUsername: String) {
        viewModelScope.launch {
            _deleteAccountState.value = DeleteAccountState.Loading
            try {
                RetrofitClient.api.deleteAccount(confirmUsername.trim())
                // Account is gone — clear the session like a logout.
                tokenStore.clearToken()
                RetrofitClient.token = null
                _deleteAccountState.value = DeleteAccountState.Success
            } catch (e: HttpException) {
                _deleteAccountState.value = DeleteAccountState.Error(parseHttpError(e))
            } catch (e: Exception) {
                _deleteAccountState.value = DeleteAccountState.Error("Couldn't delete account. Check your connection.")
            }
        }
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = DeleteAccountState.Idle
    }

    fun register(email: String, password: String, username: String) {
        if (username.isBlank()) {
            _state.value = AuthState.Error("Username cannot be empty")
            return
        }
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
                val response = RetrofitClient.api.register(RegisterRequest(email, password, username.trim()))
                RetrofitClient.token = response.token
                tokenStore.saveToken(response.token)
                _state.value = AuthState.Success(response.token)
            } catch (e: HttpException) {
                _state.value = AuthState.Error(parseHttpError(e))
            } catch (e: Exception) {
                _state.value = AuthState.Error("Something went wrong. Check your connection.")
            }
        }
    }
}

// Supplies the TokenStore dependency to AuthViewModel via Compose's viewModel(factory = ...).
class AuthViewModelFactory(private val tokenStore: TokenStore) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(tokenStore) as T
}