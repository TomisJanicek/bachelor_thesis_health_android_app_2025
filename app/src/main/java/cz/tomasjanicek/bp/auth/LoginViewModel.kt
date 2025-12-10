package cz.tomasjanicek.bp.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // Jednoduchý stav pro UI
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun onSignInClick(context: Context) {
        if (_loginState.value is LoginState.Loading) return
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = repository.signInWithGoogle(context)

            result.onSuccess {
                _loginState.value = LoginState.Success
            }.onFailure { error ->
                _loginState.value = LoginState.Error(error.message ?: "Neznámá chyba při přihlášení")
            }
        }
    }
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
    fun onContinueAsGuestClick(onSuccess: () -> Unit) {
        // Nastavíme, že jsme host
        repository.setGuestMode(true)
        // A rovnou jdeme dál
        onSuccess()
    }
}

// Sealed class pro stav
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String?) : LoginState()
}