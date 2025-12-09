package cz.tomasjanicek.bp.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.collections.find
import kotlin.collections.mapNotNull
import kotlin.collections.toMutableSet
import android.content.Context
import android.net.Uri
import android.util.Log
import cz.tomasjanicek.bp.services.PdfExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val categoriesRepo: ILocalMeasurementCategoriesRepository,
    private val measurementsRepo: ILocalMeasurementsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ========== LOG TAG PRO SNADNÉ FILTROVÁNÍ V LOGCAT ==========
    private val TAG = "PDF_EXPORT_LOG"
    // =============================================================

    private val _filters = MutableStateFlow(StatsState())

    private val _exportedFileUri = MutableStateFlow<Uri?>(null)
    val exportedFileUri: StateFlow<Uri?> = _exportedFileUri

    val uiState: StateFlow<StatsState> = _filters
        .flatMapLatest { filters ->
            // ... (tento blok je beze změny)
            val (start, end) = calculateDateRange(filters.selectedPeriodType, filters.customStartDate, filters.customEndDate)
            combine(
                categoriesRepo.getAllCategoriesWithFields(),
                measurementsRepo.getMeasurementsWithValuesBetween(start, end)
            ) { allCategoriesWithFields, measurementsInRange ->
                val chartDataList = filters.selectedCategoryIds.mapNotNull { catId ->
                    val categoryWithFields = allCategoriesWithFields.find { it.category.id == catId }
                    if (categoryWithFields != null) {
                        val relevantMeasurements = measurementsInRange.filter { it.measurement.categoryId == catId }
                        StatsChartData(categoryWithFields, relevantMeasurements)
                    } else {
                        null
                    }
                }
                filters.copy(
                    isLoading = false,
                    allCategories = allCategoriesWithFields.map { it.category },
                    chartData = chartDataList
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatsState()
        )

    fun onAction(action: StatsAction) {
        when (action) {
            // ... (ostatní větve beze změny)
            is StatsAction.OnCategorySelectionChanged -> {
                _filters.update { state ->
                    val newSelection = state.selectedCategoryIds.toMutableSet()
                    if (action.isSelected) newSelection.add(action.categoryId)
                    else newSelection.remove(action.categoryId)
                    state.copy(selectedCategoryIds = newSelection)
                }
            }
            is StatsAction.OnPeriodTypeChanged -> {
                _filters.update { it.copy(selectedPeriodType = action.periodType) }
            }
            is StatsAction.OnCustomStartDateChanged -> {
                _filters.update { it.copy(customStartDate = action.date) }
            }
            is StatsAction.OnCustomEndDateChanged -> {
                _filters.update { it.copy(customEndDate = action.date) }
            }
            is StatsAction.OnExportClicked -> {
                exportDataToPdf()
            }
        }
    }

    private fun exportDataToPdf() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState.chartData.isNotEmpty()) {
                Log.d(TAG, "[VM-A] Akce OnExportClicked přijata, spouštím `exportDataToPdf`...")
                val exporter = PdfExporter(context)
                val uri = exporter.exportStatsToPdf(
                    chartData = currentState.chartData,
                    periodType = currentState.selectedPeriodType,
                    startDate = currentState.customStartDate,
                    endDate = currentState.customEndDate
                )

                if (uri != null) {
                    Log.d(TAG, "[VM-B] Exportér vrátil platné URI: $uri. Nastavuji ho do StateFlow.")
                } else {
                    Log.e(TAG, "[VM-B] Exportér vrátil NULL! Zkontrolujte logy z `PdfExporter`.")
                }
                _exportedFileUri.value = uri
            } else {
                Log.w(TAG, "[VM-A] Export zrušen, nejsou žádná data k exportu (chartData je prázdný).")
            }
        }
    }

    fun onExportHandled() {
        Log.d(TAG, "[VM-C] Akce `onExportHandled` zavolána z UI, resetuji URI na null.")
        _exportedFileUri.value = null
    }

    private fun calculateDateRange(
        periodType: StatsPeriodType,
        customStart: LocalDate,
        customEnd: LocalDate
    ): Pair<Long, Long> {
        // ... (tato funkce je beze změny)
        val today = LocalDate.now()
        val start: LocalDate = when (periodType) {
            StatsPeriodType.DAY -> today
            StatsPeriodType.WEEK -> today.minusWeeks(1).plusDays(1)
            StatsPeriodType.MONTH -> today.minusMonths(1).plusDays(1)
            StatsPeriodType.YEAR -> today.minusYears(1).plusDays(1)
            StatsPeriodType.CUSTOM -> customStart
        }
        val end: LocalDate = if (periodType == StatsPeriodType.CUSTOM) customEnd else today

        val startMillis = start.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val endMillis = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        return Pair(startMillis, endMillis)
    }
}