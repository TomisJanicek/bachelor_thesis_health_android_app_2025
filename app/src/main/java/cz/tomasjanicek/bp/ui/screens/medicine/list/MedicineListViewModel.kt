package cz.tomasjanicek.bp.ui.screens.medicine.list

import androidx.compose.foundation.gestures.forEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.medicine.IMedicineRepository
import cz.tomasjanicek.bp.model.Medicine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineListViewModel @Inject constructor(
    private val repository: IMedicineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicineListState())
    val uiState = _uiState.asStateFlow()

    init {
        // Pozorujeme změnu vybraného data ve stavu
        viewModelScope.launch {
            uiState.collect { state ->
                // Vždy když se změní `selectedDate`, spustí se tato vnitřní korutina
                launch {
                    combine(
                        repository.getPlannedRemindersForDate(state.selectedDate), // Použijeme nové funkce
                        repository.getCompletedRemindersForDate(state.selectedDate)
                    ) { planned, completed ->
                        val allMedicineIds = (planned.map { it.medicineId } + completed.map { it.medicineId }).toSet()
                        val detailsMap = mutableMapOf<Long, Medicine>()
                        allMedicineIds.forEach { id ->
                            repository.getMedicineByIdOnce(id)?.let { medicine ->
                                detailsMap[id] = medicine
                            }
                        }

                        // Aktualizujeme zbytek UI State, datum se nemění
                        _uiState.value = _uiState.value.copy(
                            todaysPlanned = planned,
                            todaysCompleted = completed,
                            medicineDetails = detailsMap
                        )
                    }.collect()
                }
            }
        }
    }

    fun onAction(action: MedicineListAction) {
        when (action) {
            is MedicineListAction.OnReminderToggled -> {
                viewModelScope.launch {
                    repository.updateReminderStatus(action.reminderId, action.isCompleted)
                }
            }
            is MedicineListAction.OnDeleteMedicineClicked -> {
                viewModelScope.launch {
                    repository.deleteMedicineAndReminders(action.medicineId)
                }
            }
            is MedicineListAction.OnDateChanged -> {
                // Jen aktualizujeme datum ve stavu, `init` blok se postará o zbytek
                _uiState.value = _uiState.value.copy(selectedDate = action.newDate)
            }
        }
    }
}