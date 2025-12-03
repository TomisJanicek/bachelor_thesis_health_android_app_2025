package cz.tomasjanicek.bp.ui.screens.examination.list

import cz.tomasjanicek.bp.model.ExaminationWithDoctor

sealed class ListOfExaminationUIState {
    object Loading : ListOfExaminationUIState()
    data class Success(
        val scheduledExaminations: List<ExaminationWithDoctor>,
        val historyExaminations: List<ExaminationWithDoctor>
    ) : ListOfExaminationUIState()
}