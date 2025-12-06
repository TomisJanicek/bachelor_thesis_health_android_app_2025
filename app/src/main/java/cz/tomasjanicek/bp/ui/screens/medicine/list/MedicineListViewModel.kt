package cz.tomasjanicek.bp.ui.screens.medicine.list

import androidx.compose.foundation.gestures.forEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.medicine.IMedicineRepository
import cz.tomasjanicek.bp.model.Medicine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineListViewModel @Inject constructor(
    private val repository: IMedicineRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(MedicineListState().selectedDate)

    val uiState: StateFlow<MedicineListState> = _selectedDate
        .flatMapLatest { date ->
            // Kdykoliv se _selectedDate změní, tato část se spustí znovu
            // a starý `combine` se automaticky zruší.
            combine(
                repository.getPlannedRemindersForDate(date),
                repository.getCompletedRemindersForDate(date)
            ) { planned, completed ->
                val allMedicineIds = (planned.map { it.medicineId } + completed.map { it.medicineId }).toSet()
                val detailsMap = mutableMapOf<Long, Medicine>()
                allMedicineIds.forEach { id ->
                    repository.getMedicineByIdOnce(id)?.let { medicine ->
                        detailsMap[id] = medicine
                    }
                }
                // Vytvoříme kompletní nový stav
                MedicineListState(
                    selectedDate = date,
                    todaysPlanned = planned,
                    todaysCompleted = completed,
                    medicineDetails = detailsMap
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MedicineListState()
        )

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
                // Pouze změníme hodnotu v našem pomocném flow
                _selectedDate.update { action.newDate }
            }
        }
    }
}