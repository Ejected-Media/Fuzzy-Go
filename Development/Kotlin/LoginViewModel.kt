package com.ejectedmedia.fuzzygo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejectedmedia.fuzzygo.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun handleGoogleIdToken(idToken: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val success = repository.signInWithGoogle(idToken)
            if (success) {
                _loginState.value = LoginState.Success
            } else {
                _loginState.value = LoginState.Error("Authentication Failed")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState() // This triggers navigation
    data class Error(val message: String) : LoginState()
}
