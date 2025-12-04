package cz.tomasjanicek.bp.ui.screens.measurement.addEditMeasurement

import androidx.annotation.StringRes
import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField

data class MeasurementFieldUi(
    val field: MeasurementCategoryField,
    val valueText: String = "",
    @StringRes val error: Int? = null
)

data class AddEditMeasurementData(
    val measurement: Measurement = Measurement(
        id = 0,
        categoryId = 0,
        measuredAt = System.currentTimeMillis()
    ),
    val category: MeasurementCategory? = null,
    val fields: List<MeasurementFieldUi> = emptyList()
)