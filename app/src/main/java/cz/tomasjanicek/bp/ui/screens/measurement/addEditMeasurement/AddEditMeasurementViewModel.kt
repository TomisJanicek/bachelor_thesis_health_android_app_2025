package cz.tomasjanicek.bp.ui.screens.measurement.addEditMeasurement

import androidx.core.graphics.values
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementsRepository
import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.model.MeasurementValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Definujeme si typ pro jednorázové eventy
sealed class AddEditMeasurementEvent {
    data object NavigateBack : AddEditMeasurementEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddEditMeasurementViewModel @Inject constructor(
    private val measurementsRepository: ILocalMeasurementsRepository,
    private val categoriesRepository: ILocalMeasurementCategoriesRepository
) : ViewModel(), AddEditMeasurementAction {

    private data class InputIds(val categoryId: Long, val measurementId: Long?)

    private val idsFlow = MutableStateFlow(InputIds(0L, null))
    private val localChangesFlow = MutableStateFlow<AddEditMeasurementData?>(null)

    // Event Flow pro jednorázové akce (navigace)
    private val _eventFlow = MutableSharedFlow<AddEditMeasurementEvent>()
    val eventFlow: SharedFlow<AddEditMeasurementEvent> = _eventFlow.asSharedFlow()


    val uiState: StateFlow<AddEditMeasurementUIState> =
        idsFlow.flatMapLatest { ids ->
            if (ids.categoryId <= 0) {
                return@flatMapLatest kotlinx.coroutines.flow.flowOf(AddEditMeasurementUIState.Loading)
            }

            combine(
                categoriesRepository.getCategoryWithFieldsById(ids.categoryId),
                // OPRAVA 1: Použijeme neplatné ID, pokud je measurementId null
                measurementsRepository.getMeasurementWithValuesById(ids.measurementId ?: -1L),
                localChangesFlow
            ) { categoryWithFields, measurementWithValues, localData ->

                if (categoryWithFields == null) {
                    return@combine AddEditMeasurementUIState.Loading
                }

                if (localData != null && localData.measurement.categoryId == ids.categoryId) {
                    return@combine AddEditMeasurementUIState.MeasurementChanged(localData)
                }

                val measurement = measurementWithValues?.measurement ?: Measurement(
                    id = 0,
                    categoryId = categoryWithFields.category.id,
                    measuredAt = System.currentTimeMillis()
                )

                val fieldsUi = categoryWithFields.fields.map { field ->
                    val valueForField = measurementWithValues?.values?.firstOrNull {
                        it.categoryFieldId == field.id
                    }
                    val valueString = valueForField?.value?.toString() ?: ""
                    MeasurementFieldUi(
                        field = field,
                        valueText = if (valueString.endsWith(".0")) valueString.removeSuffix(".0") else valueString
                    )
                }

                val finalData = AddEditMeasurementData(
                    measurement = measurement,
                    category = categoryWithFields.category,
                    fields = fieldsUi
                )

                localChangesFlow.value = finalData

                AddEditMeasurementUIState.MeasurementChanged(finalData)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AddEditMeasurementUIState.Loading
        )

    fun loadMeasurement(categoryId: Long, measurementId: Long?) {
        idsFlow.value = InputIds(categoryId, measurementId)
        localChangesFlow.value = null
    }

    override fun saveMeasurement() {
        viewModelScope.launch {
            val currentData = localChangesFlow.value ?: return@launch
            var hasError = false

            val newFields = currentData.fields.map { fieldUi ->
                val value = fieldUi.valueText.trim().replace(",", ".")
                if (value.isBlank() || value.toDoubleOrNull() == null) {
                    hasError = true
                    fieldUi.copy(error = R.string.error_field_type)
                } else {
                    fieldUi.copy(error = null)
                }
            }

            if (hasError) {
                localChangesFlow.value = currentData.copy(fields = newFields)
                return@launch
            }

            val values = newFields.map { fieldUi ->
                MeasurementValue(
                    measurementId = currentData.measurement.id,
                    categoryFieldId = fieldUi.field.id,
                    value = fieldUi.valueText.trim().replace(",", ".").toDouble()
                )
            }

            if (currentData.measurement.id == 0L) {
                measurementsRepository.insertMeasurementWithValues(currentData.measurement, values)
            } else {
                measurementsRepository.updateMeasurementWithValues(currentData.measurement, values)
            }

            // OPRAVA 2: Odešleme jednorázový event pro navigaci zpět
            _eventFlow.emit(AddEditMeasurementEvent.NavigateBack)
        }
    }

    override fun deleteMeasurement() {
        viewModelScope.launch {
            val currentData = localChangesFlow.value
            if (currentData != null && currentData.measurement.id != 0L) {
                measurementsRepository.deleteMeasurement(currentData.measurement)
                // OPRAVA 2: Odešleme jednorázový event pro navigaci zpět
                _eventFlow.emit(AddEditMeasurementEvent.NavigateBack)
            }
        }
    }

    override fun onDateTimeChanged(dateTime: Long) {
        val currentData = localChangesFlow.value ?: return
        localChangesFlow.value = currentData.copy(
            measurement = currentData.measurement.copy(measuredAt = dateTime)
        )
    }

    override fun onFieldValueChanged(fieldId: Long, value: String) {
        val currentData = localChangesFlow.value ?: return
        val newFields = currentData.fields.map {
            if (it.field.id == fieldId) {
                it.copy(valueText = value, error = null)
            } else {
                it
            }
        }
        localChangesFlow.value = currentData.copy(fields = newFields)
    }
}