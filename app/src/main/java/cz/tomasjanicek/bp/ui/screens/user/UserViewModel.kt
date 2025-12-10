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

    // Stav držící data uživatele (Jméno, Email, Fotka)
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    // NOVÉ: Stav pro indikaci odhlašování
    private val _isSigningOut = MutableStateFlow(false)
    val isSigningOut = _isSigningOut.asStateFlow()

    init {
        // Hned při startu ViewModelu si vytáhneme aktuálního uživatele z Repository
        _currentUser.value = repository.getCurrentUser()
    }

    // Metoda pro odhlášení
    fun onSignOutClick(onSignOutCompleted: () -> Unit) {
        viewModelScope.launch {
            // 1. Zobrazíme loading
            _isSigningOut.value = true

            // 2. Provedeme asynchronní odhlášení (toto trvá)
            repository.signOut()

            // 3. Hotovo -> navigace
            onSignOutCompleted()

            // (Volitelně vrátíme stav zpět, ale to už budeme na jiné obrazovce)
            _isSigningOut.value = false
        }
    }
}