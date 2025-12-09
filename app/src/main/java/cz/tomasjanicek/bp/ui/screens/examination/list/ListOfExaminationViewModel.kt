package cz.tomasjanicek.bp.ui.screens.examination.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.database.injection.IInjectionRepository
import cz.tomasjanicek.bp.model.ExaminationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ListOfExaminationViewModel @Inject constructor(
    private val examinationsRepository: ILocalExaminationsRepository,
    private val injectionsRepository: IInjectionRepository
) : ViewModel() {

    // Vystavíme UIState jako jeden neměnný StateFlow, který se automaticky aktualizuje
    val listOfExaminationUIState: StateFlow<ListOfExaminationUIState> =
        // zkombinujeme oba streamy dat
        combine(
            examinationsRepository.getAllWithDoctors(),
            injectionsRepository.getAllInjections()
        ) { examinationsWithDoctors, allInjections ->
            val now = System.currentTimeMillis()

            val updatedExaminations = examinationsWithDoctors.map { examWithDoctor ->
                // ... (vaše stávající logika pro overdue zůstává)
                val exam = examWithDoctor.examination
                if (exam.status == ExaminationStatus.PLANNED && exam.dateTime < now) {
                    examWithDoctor.copy(examination = exam.copy(status = ExaminationStatus.OVERDUE))
                } else {
                    examWithDoctor
                }
            }

            val scheduled = updatedExaminations.filter {
                it.examination.status == ExaminationStatus.PLANNED || it.examination.status == ExaminationStatus.OVERDUE
            }

            val history = updatedExaminations.filter {
                it.examination.status == ExaminationStatus.COMPLETED || it.examination.status == ExaminationStatus.CANCELLED
            }

            val sortedScheduled = scheduled.sortedWith(
                compareBy<cz.tomasjanicek.bp.model.ExaminationWithDoctor> {
                    it.examination.status != ExaminationStatus.OVERDUE
                }.thenBy {
                    it.examination.dateTime
                }
            )

            val sortedHistory = history.sortedByDescending { it.examination.dateTime }

            // Vytvoříme finální stav s VŠEMI daty
            ListOfExaminationUIState.Success(
                scheduledExaminations = sortedScheduled,
                historyExaminations = sortedHistory,
                allInjections = allInjections // <-- Předáme očkování
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ListOfExaminationUIState.Loading
            )
}