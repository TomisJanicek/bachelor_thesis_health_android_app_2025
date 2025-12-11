package cz.tomasjanicek.bp.ui.screens.examination.addEdit

import androidx.compose.animation.core.copy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.ExaminationStatus
import cz.tomasjanicek.bp.model.ExaminationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import cz.tomasjanicek.bp.R

@HiltViewModel
class AddEditExaminationViewModel @Inject constructor(
    private val examinationRepository: ILocalExaminationsRepository,
    private val doctorRepository: ILocalDoctorsRepository
) : ViewModel(), AddEditExaminationAction {

    private val _addEditExaminationUIState: MutableStateFlow<AddEditExaminationUIState> =
        MutableStateFlow(AddEditExaminationUIState.Loading)

    val addEditExaminationUIState = _addEditExaminationUIState.asStateFlow()

    // Smazaná nepotřebná proměnná 'data'

    override fun saveExamination() {
        viewModelScope.launch {
            val currentState = _addEditExaminationUIState.value
            if (currentState !is AddEditExaminationUIState.ExaminationChanged) return@launch

            val data = currentState.data
            var isValid = true

            // --- VALIDACE ÚČELU ---
            val purposeError = if (data.examination.purpose.isBlank()) {
                isValid = false
                R.string.error_field_required // Předpokládá, že máte tento string v resources
            } else {
                null
            }

            // --- VALIDACE LÉKAŘE ---
            val doctorError = if (data.examination.doctorId == null) {
                isValid = false
                R.string.error_field_required
            } else {
                null
            }

            if (isValid) {
                // Pokud je vše v pořádku, uložíme
                examinationRepository.insert(data.examination)
                _addEditExaminationUIState.value = AddEditExaminationUIState.ExaminationSaved
            } else {
                // Pokud jsou chyby, aktualizujeme stav a zobrazíme je v UI
                _addEditExaminationUIState.update {
                    (it as AddEditExaminationUIState.ExaminationChanged).copy(
                        data = it.data.copy(
                            purposeError = purposeError,
                            doctorError = doctorError,
                            dateTimeError = null
                        )
                    )
                }
            }
        }
    }

    override fun deleteExamination() {
        viewModelScope.launch {
            val currentState = _addEditExaminationUIState.value
            if (currentState is AddEditExaminationUIState.ExaminationChanged) {
                // Přistupujeme k datům bezpečně přes stav
                examinationRepository.delete(currentState.data.examination)
                _addEditExaminationUIState.value = AddEditExaminationUIState.ExaminationDeleted
            }
        }
    }

    fun loadExamination(id: Long?) {
        viewModelScope.launch {
            // Zkontrolujte název metody v repozitáři (předpoklad: getAllDoctors)
            val allDoctors = doctorRepository.getAll().first()
            val examinationData = if (id != null) {
                // Režim úprav: načteme existující vyšetření
                examinationRepository.getExamination(id)
            } else {
                // Režim přidání: vytvoříme nové, prázdné vyšetření
                AddEditExaminationData().examination
            }

            // Vytvoříme počáteční stav s načtenými daty
            _addEditExaminationUIState.value = AddEditExaminationUIState.ExaminationChanged(
                AddEditExaminationData(
                    examination = examinationData,
                    doctors = allDoctors // Vložíme seznam doktorů
                )
            )
        }
    }

    // --- Implementace chybějících metod ---

    private fun updateState(updateBlock: (AddEditExaminationData) -> AddEditExaminationData) {
        _addEditExaminationUIState.update { currentState ->
            if (currentState is AddEditExaminationUIState.ExaminationChanged) {
                currentState.copy(data = updateBlock(currentState.data))
            } else {
                currentState
            }
        }
    }

    override fun onPurposeChanged(purpose: String) {
        updateState {
            it.copy(
                examination = it.examination.copy(purpose = purpose),
                purposeError = null // <--- TOTO JE KLÍČOVÉ: Vymazat chybu při psaní
            )
        }
    }

    override fun onTypeChanged(type: ExaminationType) {
        updateState { it.copy(examination = it.examination.copy(type = type)) }
    }

    override fun onDateTimeChanged(dateTime: Long) {
        updateState {
            it.copy(
                examination = it.examination.copy(dateTime = dateTime),
                dateTimeError = null // <--- Vymazat chybu při změně času
            )
        }
    }

    override fun onDoctorChanged(doctorId: Long) {
        updateState {
            it.copy(
                examination = it.examination.copy(doctorId = doctorId),
                doctorError = null // <--- Vymazat chybu při výběru lékaře
            )
        }
    }

    override fun onNoteChanged(note: String) {
        updateState { it.copy(examination = it.examination.copy(note = note)) }
    }

    override fun onResultChanged(result: String) {
        updateState { it.copy(examination = it.examination.copy(result = result)) }
    }

    override fun onStatusChanged(status: ExaminationStatus) {
        updateState { it.copy(examination = it.examination.copy(status = status)) }
    }
}