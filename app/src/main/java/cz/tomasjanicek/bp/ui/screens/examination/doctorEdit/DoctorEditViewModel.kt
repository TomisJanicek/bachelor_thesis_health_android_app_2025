package cz.tomasjanicek.bp.ui.screens.examination.doctorEdit

import androidx.activity.result.launch
import androidx.compose.animation.core.copy
import androidx.compose.ui.test.cancel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.geo.getAddressFromCoordinates
import cz.tomasjanicek.bp.navigation.INavigationRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class DoctorEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context, // <-- ZMĚNA ZDE
    private val doctorRepository: ILocalDoctorsRepository
) : ViewModel(), DoctorEditAction {

    private val _uiState = MutableStateFlow<DoctorEditUIState>(DoctorEditUIState.Loading)
    val uiState = _uiState.asStateFlow()

    fun handleLocationResult(latitude: Double, longitude: Double) {
        Log.d("LocationFlow", "[DoctorEditViewModel] handleLocationResult...")

        // 1. Aktualizace souřadnic
        updateState { currentData ->
            currentData.copy(
                doctor = currentData.doctor?.copy(latitude = latitude, longitude = longitude)
            )
        }

        viewModelScope.launch {
            Log.d("LocationFlow", "[DoctorEditViewModel] Spouštím reverse geocoding...")
            val address = getAddressFromCoordinates(context, latitude, longitude)
            Log.d("LocationFlow", "[DoctorEditViewModel] Výsledek geocodingu: ${address ?: "null"}")

            if (address != null) {
                // 2. Aktualizace adresy
                updateState { currentData ->
                    currentData.copy(
                        doctor = currentData.doctor?.copy(location = address)
                    )
                }
            }
        }
    }


    fun loadDoctor(doctorId: Long) {
        viewModelScope.launch {
            _uiState.value = DoctorEditUIState.Loading
            val doctor = doctorRepository.getDoctor(doctorId)
            if (doctor != null) {
                _uiState.value = DoctorEditUIState.Success(
                    DoctorEditData(doctor = doctor)
                )
            } else {
                _uiState.value = DoctorEditUIState.Error("Lékař s ID $doctorId nebyl nalezen.")
            }
        }
    }

    // ZMĚNA: Metoda saveDoctor nyní provádí validaci a uložení
    override fun saveDoctor() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is DoctorEditUIState.Success) return@launch

            val data = currentState.data
            val doctor = data.doctor ?: return@launch
            var isValid = true

            // Validace jména
            val nameError = if (doctor.name?.isBlank() == true) {
                isValid = false
                R.string.error_field_required
            } else {
                null
            }

            if (isValid) {
                // Pokud je vše v pořádku, uložíme
                doctorRepository.update(doctor)
                _uiState.value = DoctorEditUIState.DoctorSaved
            } else {
                // Jinak aktualizujeme UI s chybami
                _uiState.update {
                    (it as DoctorEditUIState.Success).copy(
                        data = it.data.copy(nameError = nameError)
                    )
                }
            }
        }
    }

    // ZMĚNA: Univerzální update funkce, která jen mění stav, NEUKLÁDÁ
    private fun updateState(updateAction: (DoctorEditData) -> DoctorEditData) {
        _uiState.update { currentState ->
            // Aktualizujeme pouze pokud jsme ve stavu Success
            if (currentState is DoctorEditUIState.Success) {
                // Zavoláme lambda funkci, která nám vrátí nová data
                val newData = updateAction(currentState.data)
                // Vrátíme nový Success stav s aktualizovanými daty
                currentState.copy(data = newData)
            } else {
                // Pokud nejsme ve stavu Success (např. Loading), nic neměníme
                currentState
            }
        }
    }

    // ZMĚNA: Všechny 'on...Changed' metody nyní pouze volají 'updateState'
    override fun onNameChanged(name: String) {
        updateState { it.copy(doctor = it.doctor?.copy(name = name), nameError = null) }
    }

    override fun onPhoneChanged(phone: String) {
        updateState { it.copy(doctor = it.doctor?.copy(phone = phone.ifBlank { null })) }
    }

    override fun onEmailChanged(email: String) {
        updateState { it.copy(doctor = it.doctor?.copy(email = email.ifBlank { null })) }
    }

    override fun onLocationChanged(location: String) {
        updateState { it.copy(doctor = it.doctor?.copy(location = location.ifBlank { null })) }
    }

    override fun onSubtitleChanged(subtitle: String) {
        updateState { it.copy(doctor = it.doctor?.copy(subtitle = subtitle.ifBlank { null })) }
    }

}