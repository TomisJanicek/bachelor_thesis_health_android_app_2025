package cz.tomasjanicek.bp.ui.screens.examination.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.model.ExaminationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ListOfExaminationViewModel @Inject constructor(
    private val repository: ILocalExaminationsRepository
) : ViewModel() {

    // Vystavíme UIState jako jeden neměnný StateFlow, který se automaticky aktualizuje
    val listOfExaminationUIState: StateFlow<ListOfExaminationUIState> =
        repository.getAllWithDoctors()
            .map { examinationsWithDoctors ->
                val now = System.currentTimeMillis()

                // 1. KROK: Převedeme prošvihlé PLANNED na dočasný stav OVERDUE
                val updatedExaminations = examinationsWithDoctors.map { examWithDoctor ->
                    val exam = examWithDoctor.examination
                    if (exam.status == ExaminationStatus.PLANNED && exam.dateTime < now) {
                        // Vytvoříme kopii s novým, dočasným stavem OVERDUE
                        examWithDoctor.copy(examination = exam.copy(status = ExaminationStatus.OVERDUE))
                    } else {
                        examWithDoctor
                    }
                }

                // 2. KROK: Rozdělíme prohlídky do dvou skupin
                val scheduled = updatedExaminations.filter {
                    it.examination.status == ExaminationStatus.PLANNED || it.examination.status == ExaminationStatus.OVERDUE
                }

                val history = updatedExaminations.filter {
                    it.examination.status == ExaminationStatus.COMPLETED || it.examination.status == ExaminationStatus.CANCELLED
                }

                // 3. KROK: Seřadíme každou skupinu podle vašich pravidel
                val sortedScheduled = scheduled.sortedWith(
                    // Vytvoříme víceúrovňový komparátor
                    compareBy<cz.tomasjanicek.bp.model.ExaminationWithDoctor> {
                        // První úroveň: OVERDUE (true) má přednost před PLANNED (false)
                        it.examination.status != ExaminationStatus.OVERDUE
                    }.thenBy {
                        // Druhá úroveň: Seřadíme podle data (vzestupně od nejbližšího)
                        it.examination.dateTime
                    }
                )

                val sortedHistory = history.sortedByDescending { it.examination.dateTime }

                // 4. KROK: Vytvoříme finální stav s oběma připravenými seznamy
                ListOfExaminationUIState.Success(
                    scheduledExaminations = sortedScheduled,
                    historyExaminations = sortedHistory
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ListOfExaminationUIState.Loading
            )
}