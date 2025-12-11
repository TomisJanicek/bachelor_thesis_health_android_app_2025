package cz.tomasjanicek.bp.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.auth.AuthRepository
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val doctorRepository: ILocalDoctorsRepository // <-- Přidáno: Potřebujeme přístup k doktorům
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val destination = _destination.asStateFlow()

    init {
        decideNavigation()
    }

    private fun decideNavigation() {
        viewModelScope.launch {
            // 1. DŮLEŽITÉ: Inicializace databáze doktorů
            // Pokud je tabulka prázdná (první spuštění nebo po resetu), naplníme ji.
            // Toto se provede rychle na pozadí.
            doctorRepository.initializeDoctorsIfEmpty()

            // 2. Zdržení pro logo (UX - aby logo jen neprobliklo)
            delay(1000)

            // 3. Kontrola přihlášení
            val isLoggedIn = repository.getCurrentUser() != null
            val isGuest = repository.isGuestMode()

            if (isLoggedIn || isGuest) {
                _destination.value = SplashDestination.Home
            } else {
                _destination.value = SplashDestination.Login
            }
        }
    }
}

enum class SplashDestination {
    None, Home, Login
}