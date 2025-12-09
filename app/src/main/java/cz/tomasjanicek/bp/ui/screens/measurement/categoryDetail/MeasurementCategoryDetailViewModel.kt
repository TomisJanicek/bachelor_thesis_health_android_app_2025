package cz.tomasjanicek.bp.ui.screens.measurement.categoryDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementsRepository
import cz.tomasjanicek.bp.model.Measurement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MeasurementCategoryDetailViewModel @Inject constructor(
    private val categoriesRepository: ILocalMeasurementCategoriesRepository,
    private val measurementsRepository: ILocalMeasurementsRepository
) : ViewModel() {

    private val categoryIdFlow = MutableStateFlow<Long>(0L)

    val uiState: StateFlow<MeasurementCategoryDetailUIState> =
        categoryIdFlow.flatMapLatest { id ->
            if (id <= 0L) {
                return@flatMapLatest kotlinx.coroutines.flow.flowOf(MeasurementCategoryDetailUIState.Error)
            }

            combine(
                categoriesRepository.getCategoryWithFieldsById(id),
                measurementsRepository.getMeasurementsByCategory(id),
                measurementsRepository.getValuesByCategory(id)
            ) { categoryWithFields, measurements, rawValues ->

                if (categoryWithFields == null) {
                    return@combine MeasurementCategoryDetailUIState.Error
                }

                val valuesByMeasurementId: Map<Long, List<MeasurementValueDisplay>> =
                    measurements
                        .filter { it.id != 0L }
                        .associate { measurement ->
                            val mValues = rawValues.filter { it.measurementId == measurement.id }
                            val displayList = mValues.map { v ->
                                val field = categoryWithFields.fields.firstOrNull { f -> f.id == v.categoryFieldId }
                                // --- ZDE JE OPRAVA ---
                                MeasurementValueDisplay(
                                    fieldId = v.categoryFieldId,         // <-- PŘIDÁNO
                                    measurementId = v.measurementId,   // <-- PŘIDÁNO
                                    label = field?.label ?: "Hodnota",
                                    unit = field?.unit,
                                    value = v.value
                                )
                            }
                            measurement.id to displayList
                        }

                MeasurementCategoryDetailUIState.Content(
                    category = categoryWithFields.category,
                    measurements = measurements,
                    fields = categoryWithFields.fields,
                    valuesByMeasurementId = valuesByMeasurementId
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MeasurementCategoryDetailUIState.Loading
        )

    fun load(categoryId: Long) {
        categoryIdFlow.value = categoryId
    }

    fun deleteMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            try {
                measurementsRepository.deleteMeasurement(measurement)
            } catch (e: Exception) {
                // Logování chyby
            }
        }
    }
}