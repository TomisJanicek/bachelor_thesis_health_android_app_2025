package cz.tomasjanicek.bp.ui.screens.medicine.list

import androidx.compose.foundation.gestures.forEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.medicine.IMedicineRepository
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.ReminderStatus
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
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MedicineListViewModel @Inject constructor(
    private val repository: IMedicineRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now()) // Změna: inicializace přímo datem

    val uiState: StateFlow<MedicineListState> = _selectedDate
        .flatMapLatest { date ->
            combine(
                repository.getPlannedRemindersForDate(date),
                repository.getCompletedRemindersForDate(date)
            ) { rawPlanned, rawCompleted ->

                // 1. Spojíme oba seznamy a odstraníme duplicity podle ID
                val allReminders = (rawPlanned + rawCompleted).distinctBy { it.id }

                // 2. Rozdělíme je podle Enumu 'status'
                // ZMĚNA: Místo !it.isTaken používáme porovnání se statusem
                val planned = allReminders.filter { it.status == ReminderStatus.PLANNED }
                val completed = allReminders.filter { it.status == ReminderStatus.COMPLETED }

                // 3. Načtení detailů léků
                val detailsMap = mutableMapOf<Long, Medicine>()
                allReminders.forEach { reminder ->
                    repository.getMedicineByIdOnce(reminder.medicineId)?.let { medicine ->
                        detailsMap[reminder.medicineId] = medicine
                    }
                }

                MedicineListState(
                    isLoading = false,
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
            initialValue = MedicineListState(isLoading = true, selectedDate = LocalDate.now())
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
                _selectedDate.update { action.newDate }
            }
        }
    }
}