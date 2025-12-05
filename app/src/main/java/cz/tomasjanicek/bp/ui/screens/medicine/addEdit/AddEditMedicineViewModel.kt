package cz.tomasjanicek.bp.ui.screens.medicine.addEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.medicine.IMedicineRepository
import cz.tomasjanicek.bp.model.EndingType
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.RegularityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AddEditMedicineViewModel @Inject constructor(
    private val repository: IMedicineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditMedicineUIState())
    val uiState = _uiState.asStateFlow()

    fun loadMedicine(medicineId: Long?) {
        if (medicineId == null || medicineId == -1L) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            val medicine = repository.getMedicineById(medicineId).firstOrNull()
            if (medicine != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEditing = true,
                        medicineId = medicine.id,
                        name = medicine.name,
                        note = medicine.note.orEmpty(),
                        dosage = medicine.dosage.toString(),
                        unit = medicine.unit,
                        isRegular = medicine.isRegular,
                        regularDays = medicine.regularDays?.toSet() ?: emptySet(),
                        regularTimes = medicine.regularTimes.map { timeInMinutes ->
                            LocalTime.of(timeInMinutes / 60, timeInMinutes % 60)
                        }.toSet(),
                        startDate = medicine.startDate ?: System.currentTimeMillis(),
                        singleDates = medicine.singleDates?.toSet() ?: emptySet(),
                        // OPRAVA: Správné načtení nových polí z modelu
                        endingType = medicine.endingType,
                        endDate = medicine.endDate,
                        doseCount = medicine.doseCount?.toString() ?: ""
                    )
                }
                // Validujeme načtený stav
                _uiState.update { validate(it) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onAction(action: AddEditMedicineAction) {
        val currentState = _uiState.value
        val newState = reduce(action, currentState)
        _uiState.value = validate(newState) // Vždy validujeme nový stav
    }

    private fun reduce(action: AddEditMedicineAction, currentState: AddEditMedicineUIState): AddEditMedicineUIState {
        return when (action) {
            is AddEditMedicineAction.OnNameChanged -> currentState.copy(name = action.name)
            is AddEditMedicineAction.OnNoteChanged -> currentState.copy(note = action.note)
            is AddEditMedicineAction.OnDosageChanged -> currentState.copy(dosage = action.dosage)
            is AddEditMedicineAction.OnUnitSelected -> currentState.copy(unit = action.unit)
            is AddEditMedicineAction.OnRegularityChanged -> currentState.copy(isRegular = action.isRegular)
            is AddEditMedicineAction.OnDayOfWeekToggled -> {
                val newDays = currentState.regularDays.toMutableSet()
                if (newDays.contains(action.day)) newDays.remove(action.day) else newDays.add(action.day)
                currentState.copy(regularDays = newDays)
            }
            is AddEditMedicineAction.OnStartDateChanged -> currentState.copy(startDate = action.dateMillis)
            is AddEditMedicineAction.OnTimeAdded -> {
                val newTimes = currentState.regularTimes.toMutableSet().apply { add(action.time) }
                currentState.copy(regularTimes = newTimes)
            }
            is AddEditMedicineAction.OnTimeRemoved -> {
                val newTimes = currentState.regularTimes.toMutableSet().apply { remove(action.time) }
                currentState.copy(regularTimes = newTimes)
            }
            is AddEditMedicineAction.OnSingleDateAdded -> {
                val newDates = currentState.singleDates.toMutableSet().apply { add(action.dateTimeMillis) }
                currentState.copy(singleDates = newDates)
            }
            is AddEditMedicineAction.OnSingleDateRemoved -> {
                val newDates = currentState.singleDates.toMutableSet().apply { remove(action.dateTimeMillis) }
                currentState.copy(singleDates = newDates)
            }
            is AddEditMedicineAction.OnEndingTypeChanged -> currentState.copy(endingType = action.type)
            is AddEditMedicineAction.OnEndDateChanged -> currentState.copy(endDate = action.dateMillis)
            is AddEditMedicineAction.OnDoseCountChanged -> currentState.copy(doseCount = action.count.filter { it.isDigit() })

            is AddEditMedicineAction.OnSaveClicked -> {
                // Označíme pokus o uložení
                val stateWithAttempt = currentState.copy(hasAttemptedSave = true)
                // Pokud je validní po označení, uložíme
                if (validate(stateWithAttempt).canBeSaved) {
                    saveMedicine(stateWithAttempt)
                }
                return stateWithAttempt
            }
        }
    }

    private fun validate(state: AddEditMedicineUIState): AddEditMedicineUIState {
        val isNameValid = state.name.isNotBlank()
        val isDosageValid = (state.dosage.toDoubleOrNull() ?: 0.0) > 0.0

        val areTimesValid = if (state.isRegular) {
            val isEndingValid = when (state.endingType) {
                EndingType.INDEFINITELY -> true
                EndingType.UNTIL_DATE -> state.endDate != null && state.endDate > state.startDate
                EndingType.AFTER_DOSES -> (state.doseCount.toIntOrNull() ?: 0) > 0
            }
            state.regularTimes.isNotEmpty() && isEndingValid
        } else {
            state.singleDates.isNotEmpty()
        }

        val canBeSaved = isNameValid && isDosageValid && areTimesValid
        return state.copy(canBeSaved = canBeSaved)
    }

    private fun saveMedicine(stateToSave: AddEditMedicineUIState) {
        val calculatedRegularityType = if (stateToSave.isRegular) {
            if (stateToSave.regularDays.isEmpty() || stateToSave.regularDays.size == 7) RegularityType.DAILY else RegularityType.WEEKLY
        } else {
            RegularityType.DAILY
        }

        val medicine = Medicine(
            id = stateToSave.medicineId ?: 0,
            name = stateToSave.name.trim(),
            note = stateToSave.note.trim().takeIf { it.isNotEmpty() },
            dosage = stateToSave.dosage.toDouble(),
            unit = stateToSave.unit,
            isRegular = stateToSave.isRegular,
            regularityType = calculatedRegularityType,
            regularDays = stateToSave.regularDays.toList().sorted(),
            regularTimes = stateToSave.regularTimes.map { it.hour * 60 + it.minute }.sorted(),
            startDate = stateToSave.startDate,
            singleDates = stateToSave.singleDates.toList().sorted(),
            // OPRAVA: Správné uložení nových polí
            endingType = stateToSave.endingType,
            endDate = if (stateToSave.endingType == EndingType.UNTIL_DATE) stateToSave.endDate else null,
            doseCount = if (stateToSave.endingType == EndingType.AFTER_DOSES) stateToSave.doseCount.toIntOrNull() else null
        )

        viewModelScope.launch {
            repository.saveMedicineAndGenerateReminders(medicine)
        }
    }
}