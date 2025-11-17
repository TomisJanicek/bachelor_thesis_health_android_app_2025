package cz.tomasjanicek.bp.ui.screens.examination

import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
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
        // 1. Z repozitáře získáme Flow se spojenými daty (Examination + Doctor)
        repository.getAllWithDoctors()
            // 2. Pomocí operátoru .map() transformujeme data na náš UIState
            .map { examinations ->
                ListOfExaminationUIState.Success(examinations)
            }
            // 3. Pomocí .stateIn() převedeme výsledný Flow na StateFlow
            .stateIn(
                scope = viewModelScope, // Coroutine scope, ve kterém bude Flow aktivní
                // Začne sbírat, když je UI viditelné, a přestane 5s poté, co zmizí (šetří zdroje)
                started = SharingStarted.WhileSubscribed(5000),
                // Počáteční hodnota, než dorazí první data z databáze
                initialValue = ListOfExaminationUIState.Loading
            )
}