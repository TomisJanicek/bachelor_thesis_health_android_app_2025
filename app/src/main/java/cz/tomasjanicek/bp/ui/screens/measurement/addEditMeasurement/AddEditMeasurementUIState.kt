package cz.tomasjanicek.bp.ui.screens.measurement.addEditMeasurement

sealed class AddEditMeasurementUIState {

    data object Loading : AddEditMeasurementUIState()

    data object MeasurementSaved : AddEditMeasurementUIState()

    data object MeasurementDeleted : AddEditMeasurementUIState()

    data class MeasurementChanged(val data: AddEditMeasurementData) : AddEditMeasurementUIState()
}