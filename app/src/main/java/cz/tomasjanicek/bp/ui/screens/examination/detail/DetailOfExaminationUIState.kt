package cz.tomasjanicek.bp.ui.screens.examination.detail

import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationWithDoctor

sealed class DetailOfExaminationUIState {
    object Loading : DetailOfExaminationUIState()
    data class Loaded(val examinationWithDoctor: ExaminationWithDoctor) : DetailOfExaminationUIState()
    data class Error(val message: String) : DetailOfExaminationUIState()

    // PŘIDÁNO: Stav signalizující, že všechny záznamy byly smazány
    object AllDeleted : DetailOfExaminationUIState()
}