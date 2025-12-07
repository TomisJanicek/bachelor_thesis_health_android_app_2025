package cz.tomasjanicek.bp.ui.screens.examination.list

import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import cz.tomasjanicek.bp.model.Injection

sealed class ListOfExaminationUIState {
    object Loading : ListOfExaminationUIState()
    data class Success(
        val scheduledExaminations: List<ExaminationWithDoctor>,
        val historyExaminations: List<ExaminationWithDoctor>,
        val allInjections: List<Injection> // <-- PŘIDANÁ VLASTNOST
    ) : ListOfExaminationUIState()
}