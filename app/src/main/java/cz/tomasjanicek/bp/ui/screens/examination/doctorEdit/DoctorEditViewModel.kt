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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.distinctUntilChanged

@HiltViewModel
class DoctorEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context, // <-- ZMĚNA ZDE
    private val doctorRepository: ILocalDoctorsRepository
) : ViewModel(), DoctorEditAction {

    private val _uiState = MutableStateFlow<DoctorEditUIState>(DoctorEditUIState.Loading)
    val uiState = _uiState.asStateFlow()

    private var doctorSubscriptionJob: Job? = null

    fun handleLocationResult(latitude: Double, longitude: Double): Job {
        return viewModelScope.launch {
            Log.d("LocationFlow", "[DoctorEditViewModel] handleLocationResult start")

            // Získáme aktuální data o doktorovi, se kterými budeme pracovat.
            // Pokud data nejsou dostupná (např. stav je Loading), nemá smysl pokračovat.
            val currentDoctor = (_uiState.value as? DoctorEditUIState.Success)?.data?.doctor
            if (currentDoctor == null) {
                Log.e("LocationFlow", "handleLocationResult byl volán, ale data o doktorovi nejsou dostupná.")
                return@launch
            }

            // Vytvoříme novou, aktualizovanou instanci doktora.
            var updatedDoctor = currentDoctor.copy(latitude = latitude, longitude = longitude)

            // Krok 1: Okamžitě aktualizuj UI stav, aby uživatel viděl změnu.
            updateState { currentData ->
                currentData.copy(doctor = updatedDoctor)
            }

            // Krok 2: Spusť geocoding a počkej na něj.
            Log.d("LocationFlow", "[DoctorEditViewModel] Spouštím a čekám na reverse geocoding...")
            val address = getAddressFromCoordinates(context, latitude, longitude)
            Log.d("LocationFlow", "[DoctorEditViewModel] Výsledek geocodingu: ${address ?: "null"}")

            // Krok 3: Pokud geocoding uspěl, znovu aktualizuj instanci doktora a UI.
            if (address != null) {
                updatedDoctor = updatedDoctor.copy(addressLabel = address)
                updateState { currentData ->
                    currentData.copy(doctor = updatedDoctor)
                }
            }

            // --- KLÍČOVÝ KROK ZDE ---
            // Krok 4: Ulož finální, aktualizovanou verzi doktora do databáze.
            Log.d("LocationFlow", "[DoctorEditViewModel] Ukládám finální data doktora do databáze.")
            doctorRepository.update(updatedDoctor)

            Log.d("LocationFlow", "[DoctorEditViewModel] handleLocationResult finish")
        }
    }


    fun subscribeToDoctorUpdates(doctorId: Long) {
        doctorSubscriptionJob?.cancel() // Zrušíme staré poslouchání
        doctorSubscriptionJob = viewModelScope.launch {
            // Použijeme metodu, která vrací Flow
            doctorRepository.getDoctorWithData(doctorId)
                .distinctUntilChanged() // Ignoruje emise, pokud se data nezměnila
                .collect { doctorFromDb ->
                    if (doctorFromDb != null) {
                        // Pokaždé, když se data v DB změní, aktualizujeme náš UI stav
                        _uiState.update { currentState ->
                            // Pokud už máme nějaká data (např. validační chybu), zachováme je
                            val existingData = (currentState as? DoctorEditUIState.Success)?.data
                            DoctorEditUIState.Success(
                                data = existingData?.copy(doctor = doctorFromDb) ?: DoctorEditData(doctor = doctorFromDb)
                            )
                        }
                    } else {
                        _uiState.value = DoctorEditUIState.Error("Lékař s ID $doctorId nebyl nalezen.")
                    }
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

    override fun onLocationChanged(newLabel: String) {
        // Pouze a jen aktualizujeme textový popisek. Souřadnic se nedotýkáme.
        updateState {
            it.copy(doctor = it.doctor?.copy(addressLabel = newLabel.ifBlank { null }))
        }
    }

    override fun onSubtitleChanged(subtitle: String) {
        updateState { it.copy(doctor = it.doctor?.copy(subtitle = subtitle.ifBlank { null })) }
    }

    override fun onCleared() {
        super.onCleared()
        doctorSubscriptionJob?.cancel() // Uklidíme po sobě
    }

}