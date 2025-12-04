package cz.tomasjanicek.bp.ui.screens.measurement.categoryDetail

import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField

sealed class MeasurementCategoryDetailUIState {
    data object Loading : MeasurementCategoryDetailUIState()
    data object Error : MeasurementCategoryDetailUIState()
    data class Content(
        val category: MeasurementCategory,
        val measurements: List<Measurement>,
        val fields: List<MeasurementCategoryField>,
        val valuesByMeasurementId: Map<Long, List<MeasurementValueDisplay>>
    ) : MeasurementCategoryDetailUIState()
}