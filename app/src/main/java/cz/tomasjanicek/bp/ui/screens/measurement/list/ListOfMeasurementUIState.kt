package cz.tomasjanicek.bp.ui.screens.measurement.list

import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields

sealed class ListOfMeasurementUIState {
    data object Loading : ListOfMeasurementUIState()

    data class Content(
        val categories: List<MeasurementCategoryWithFields>
    ) : ListOfMeasurementUIState()

    data object Error : ListOfMeasurementUIState()
}