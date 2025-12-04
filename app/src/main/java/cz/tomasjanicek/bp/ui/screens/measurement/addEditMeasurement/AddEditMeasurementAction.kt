package cz.tomasjanicek.bp.ui.screens.measurement.addEditMeasurement

interface AddEditMeasurementAction {

    fun saveMeasurement()

    fun deleteMeasurement()

    fun onDateTimeChanged(dateTime: Long)

    fun onFieldValueChanged(fieldId: Long, value: String)
}