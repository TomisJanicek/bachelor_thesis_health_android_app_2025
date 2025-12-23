package cz.tomasjanicek.bp.ui.screens.examination.doctorEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.services.getAddressFromCoordinates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.util.Log
import android.util.Patterns
import cz.tomasjanicek.bp.utils.EmailValidator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.distinctUntilChanged

@HiltViewModel
class DoctorEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val doctorRepository: ILocalDoctorsRepository
) : ViewModel(), DoctorEditAction {

    private val _uiState = MutableStateFlow<DoctorEditUIState>(DoctorEditUIState.Loading)
    val uiState = _uiState.asStateFlow()

    private var doctorSubscriptionJob: Job? = null

    // --- LOGIKA PRO ZPRACOVÁNÍ POLOHY ---
    fun handleLocationResult(latitude: Double, longitude: Double): Job {
        return viewModelScope.launch {
            Log.d("LocationFlow", "[DoctorEditViewModel] handleLocationResult start")

            val currentDoctor = (_uiState.value as? DoctorEditUIState.Success)?.data?.doctor
            if (currentDoctor == null) {
                return@launch
            }

            // 1. Okamžitá aktualizace souřadnic v UI
            var updatedDoctor = currentDoctor.copy(latitude = latitude, longitude = longitude)
            updateState { currentData -> currentData.copy(doctor = updatedDoctor) }

            // 2. Geocoding (získání adresy)
            val address = getAddressFromCoordinates(context, latitude, longitude)

            // 3. Aktualizace adresy v UI (pokud se našla)
            if (address != null) {
                updatedDoctor = updatedDoctor.copy(addressLabel = address)
                updateState { currentData -> currentData.copy(doctor = updatedDoctor) }
            }

            // 4. Uložení do DB (aby se změna neztratila při rotaci nebo návratu)
            doctorRepository.update(updatedDoctor)
            Log.d("LocationFlow", "[DoctorEditViewModel] handleLocationResult finish")
        }
    }

    // --- NAČÍTÁNÍ DAT ---
    fun subscribeToDoctorUpdates(doctorId: Long) {
        doctorSubscriptionJob?.cancel()
        doctorSubscriptionJob = viewModelScope.launch {
            doctorRepository.getDoctorWithData(doctorId)
                .distinctUntilChanged()
                .collect { doctorFromDb ->
                    if (doctorFromDb != null) {
                        _uiState.update { currentState ->
                            // Zachováme existující stav (např. chyby formuláře), pokud už nějaký máme
                            val existingData = (currentState as? DoctorEditUIState.Success)?.data
                            DoctorEditUIState.Success(
                                data = existingData?.copy(doctor = doctorFromDb)
                                    ?: DoctorEditData(doctor = doctorFromDb)
                            )
                        }
                    } else {
                        _uiState.value = DoctorEditUIState.Error("Lékař s ID $doctorId nebyl nalezen.")
                    }
                }
        }
    }

    // --- ULOŽENÍ A VALIDACE ---
    override fun saveDoctor() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is DoctorEditUIState.Success) return@launch

            val data = currentState.data
            val doctor = data.doctor ?: return@launch

            // 1. Validace jména (Povinné pole)
            val nameError = if (doctor.name.isNullOrBlank()) {
                R.string.error_field_required // Ujisti se, že máš tento string v strings.xml
            } else {
                null
            }

            // 2. Validace emailu
            val emailInput = doctor.email ?: ""
            // POUŽIJEME NOVÝ VALIDÁTOR
            val isEmailValid = EmailValidator.isValid(emailInput)

            val emailError = if (!isEmailValid) {
                R.string.error_invalid_email // Ujisti se, že máš tento string v strings.xml
            } else {
                null
            }

            // 3. Rozhodnutí
            if (nameError == null && emailError == null) {
                // Vše OK -> Uložit a odejít
                doctorRepository.update(doctor)
                _uiState.value = DoctorEditUIState.DoctorSaved
            } else {
                // Chyba -> Zobrazit chyby v UI
                updateState { it.copy(nameError = nameError, emailError = emailError) }
            }
        }
    }

    // --- POMOCNÁ FUNKCE PRO AKTUALIZACI STAVU ---
    private fun updateState(updateAction: (DoctorEditData) -> DoctorEditData) {
        _uiState.update { currentState ->
            if (currentState is DoctorEditUIState.Success) {
                currentState.copy(data = updateAction(currentState.data))
            } else {
                currentState
            }
        }
    }

    // --- HANDLERY ZMĚN VE FORMULÁŘI ---

    override fun onNameChanged(name: String) {
        // Při psaní rovnou mažeme chybovou hlášku
        updateState { it.copy(doctor = it.doctor?.copy(name = name), nameError = null) }
    }

    override fun onPhoneChanged(phone: String) {
        updateState { it.copy(doctor = it.doctor?.copy(phone = phone.ifBlank { null })) }
    }

    override fun onEmailChanged(email: String) {
        // Okamžitá validace při psaní (volitelné, ale užitečné pro UX)
        val isValid = EmailValidator.isValid(email)
        val error = if (isValid) null else R.string.error_invalid_email

        updateState {
            it.copy(
                doctor = it.doctor?.copy(email = email.ifBlank { null }),
                emailError = error
            )
        }
    }

    override fun onLocationChanged(newLabel: String) {
        updateState {
            it.copy(doctor = it.doctor?.copy(addressLabel = newLabel.ifBlank { null }))
        }
    }

    override fun onSubtitleChanged(subtitle: String) {
        updateState { it.copy(doctor = it.doctor?.copy(subtitle = subtitle.ifBlank { null })) }
    }

    override fun onLocationCleared() {
        updateState { currentState ->
            currentState.copy(
                doctor = currentState.doctor?.copy(
                    addressLabel = null,
                    latitude = null,
                    longitude = null
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        doctorSubscriptionJob?.cancel()
    }
}