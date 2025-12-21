package cz.tomasjanicek.bp.ui.screens.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.auth.AuthRepository
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.model.ExaminationNotificationTime
import cz.tomasjanicek.bp.model.MedicineNotificationTime
import cz.tomasjanicek.bp.ui.elements.bottomBar.AppSection
import cz.tomasjanicek.bp.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val doctorRepository: ILocalDoctorsRepository
    // SettingsManager už tu nepotřebujeme
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val destination = _destination.asStateFlow()

    init {
        decideNavigation()
    }

    private fun decideNavigation() {
        viewModelScope.launch {
            // 1. Inicializace databáze doktorů (vždy)
            doctorRepository.initializeDoctorsIfEmpty()

            // 2. Zdržení pro logo
            delay(1000)

            // 3. Kontrola přihlášení (žádný onboarding)
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