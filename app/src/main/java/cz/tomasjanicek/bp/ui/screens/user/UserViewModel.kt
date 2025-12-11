package cz.tomasjanicek.bp.ui.screens.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import cz.tomasjanicek.bp.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isSigningOut = MutableStateFlow(false)
    val isSigningOut = _isSigningOut.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // NOVÉ: Explicitní stav pro režim hosta
    private val _isGuest = MutableStateFlow(false)
    val isGuest = _isGuest.asStateFlow()

    init {
        _currentUser.value = repository.getCurrentUser()
        // Zjistíme, jestli jsme v režimu hosta
        _isGuest.value = repository.isGuestMode()
    }

    fun onSignOutClick(onSignOutCompleted: () -> Unit) {
        viewModelScope.launch {
            _isSigningOut.value = true
            _error.value = null // Reset chyby

            try {
                // Teď je to bezpečné:
                // - Pokud je host, smaže se a projde to.
                // - Pokud je user a záloha OK, smaže se a projde to.
                // - Pokud je user a záloha FAIL, skočí to do 'catch'
                repository.signOut()

                // Když jsme tady, vše dopadlo dobře
                onSignOutCompleted()

            } catch (e: Exception) {
                // Záloha selhala -> Zůstáváme na obrazovce a ukážeme chybu
                _error.value = e.message
            } finally {
                _isSigningOut.value = false
            }
        }
    }

    // Metoda pro reset chyby (volatelné z UI po zobrazení snackbaru)
    fun clearError() {
        _error.value = null
    }
}