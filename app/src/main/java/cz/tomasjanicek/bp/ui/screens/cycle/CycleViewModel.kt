package cz.tomasjanicek.bp.ui.screens.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.cycle.ICycleRepository
import cz.tomasjanicek.bp.model.CycleRecord
import cz.tomasjanicek.bp.model.CycleSettings
import cz.tomasjanicek.bp.model.EventType
import cz.tomasjanicek.bp.ui.screens.cycle.components.Cycle
import cz.tomasjanicek.bp.ui.screens.cycle.components.CycleCalculator
import cz.tomasjanicek.bp.ui.screens.cycle.components.CyclePredictions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject


@HiltViewModel
class CycleViewModel @Inject constructor(
    private val repository: ICycleRepository,
    private val calculator: CycleCalculator
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _settings = MutableStateFlow(CycleSettings())

    private val _uiState = MutableStateFlow(CycleUIState())
    val uiState: StateFlow<CycleUIState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                repository.getAllRecords(),
                _selectedMonth,
                _settings
            ) { allRecords, month, settings ->

                val historicalCycles = calculator.groupRecordsIntoCycles(allRecords)
                val futurePredictions = calculator.predictFuture(historicalCycles, allRecords, settings)

                val (avgCycle, avgMenstruation) = calculator.calculateAverages(historicalCycles)

                val calendarDays = generateCalendarDays(month, allRecords, futurePredictions)
                val statusItems = generateCarouselItems(historicalCycles, futurePredictions)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedMonth = month,
                        calendarDays = calendarDays,
                        averageCycleLength = avgCycle,
                        averageMenstruationLength = avgMenstruation,
                        statusCarouselItems = statusItems
                    )
                }

            }.collect()
        }
    }

    fun onAction(action: CycleAction) {
        viewModelScope.launch {
            when (action) {
                is CycleAction.ShowEditDialog -> {
                    if (action.date.isAfter(LocalDate.now())) return@launch
                    val record = repository.getRecordForDate(action.date)
                    _uiState.update { it.copy(showEditDialog = EditDialogInfo(action.date, record != null)) }
                }
                is CycleAction.DismissEditDialog -> _uiState.update { it.copy(showEditDialog = null) }
                is CycleAction.LogMenstruation -> {
                    repository.saveRecord(action.date, EventType.MENSTRUATION)
                    _uiState.update { it.copy(showEditDialog = null) }
                }
                is CycleAction.LogOvulation -> {
                    repository.saveRecord(action.date, EventType.OVULATION)
                    _uiState.update { it.copy(showEditDialog = null) }
                }
                is CycleAction.DeleteEvent -> {
                    repository.deleteRecord(action.date)
                    _uiState.update { it.copy(showEditDialog = null) }
                }
                CycleAction.NextMonthClicked -> _selectedMonth.update { it.plusMonths(1) }
                CycleAction.PreviousMonthClicked -> _selectedMonth.update { it.minusMonths(1) }
            }
        }
    }

    private fun generateCalendarDays(
        month: YearMonth,
        allRecords: List<cz.tomasjanicek.bp.model.CycleRecord>,
        futurePredictions: List<CyclePredictions>
    ): List<CalendarDay> {
        val daysInMonth = month.lengthOfMonth()
        val firstDayOfWeek = month.atDay(1).dayOfWeek.value
        val emptyDays = (firstDayOfWeek - 1 + 7) % 7

        val days = mutableListOf<CalendarDay>()
        repeat(emptyDays) { days.add(CalendarDay(0, DayType.NORMAL, false)) }

        (1..daysInMonth).forEach { dayOfMonth ->
            val date = month.atDay(dayOfMonth)
            val dayType = determineDayType(date, allRecords, futurePredictions)
            days.add(CalendarDay(dayOfMonth, dayType, date.isEqual(LocalDate.now())))
        }
        return days
    }

    private fun determineDayType(
        date: LocalDate,
        allRecords: List<cz.tomasjanicek.bp.model.CycleRecord>,
        futurePredictions: List<CyclePredictions>
    ): DayType {
        // 1. Reálné záznamy mají absolutní přednost
        allRecords.find { it.date == date }?.let {
            return when (it.eventType) {
                EventType.MENSTRUATION -> DayType.MENSTRUATION
                EventType.OVULATION -> DayType.OVULATION
            }
        }

        // 2. Hledáme v predikcích, jestli datum odpovídá nějaké předpovězené události
        for (prediction in futurePredictions) {
            // Predikce pro daný den mají tuto prioritu: Ovulace > Plodné dny > Menstruace
            if (date == prediction.ovulationDay) return DayType.PREDICTED_OVULATION
            if (date in prediction.fertileWindow) return DayType.PREDICTED_FERTILE

            // --- OPRAVA ZDE ---
            // Používáme správný název pole: `fullMenstruationEstimate`
            if (date in prediction.fullMenstruationEstimate) {
                return DayType.PREDICTED_MENSTRUATION
            }
        }

        // 3. Pokud se nenašlo nic, je to normální den
        return DayType.NORMAL
    }

    private fun generateCarouselItems(
        historicalCycles: List<cz.tomasjanicek.bp.ui.screens.cycle.components.Cycle>,
        futurePredictions: List<CyclePredictions>
    ): List<String> {
        val items = mutableListOf<String>()
        val today = LocalDate.now()

        // 1. Probíhá menstruace?
        historicalCycles.find { today in it.menstruationDays }?.let { cycle ->
            val dayNumber = ChronoUnit.DAYS.between(cycle.startDate, today) + 1
            items.add("$dayNumber. den menstruace")

            val avgMenstruation = uiState.value.averageMenstruationLength.takeIf { it > 0 }
                ?: _settings.value.averageMenstruationLength.toLong()
            val remainingDays = avgMenstruation - dayNumber

            if (remainingDays > 0) {
                val dayString = if (remainingDays == 1L) "den" else if (remainingDays in 2..4) "dny" else "dní"
                items.add("Očekávané trvání: ještě $remainingDays $dayString")
            }
            return items
        }

        // 2. Ostatní stavy podle predikcí
        futurePredictions.forEach { prediction ->
            if (today == prediction.ovulationDay) {
                items.add("Pravděpodobná ovulace")
            } else if (today in prediction.fertileWindow) {
                items.add("Plodné období")
            }

            // --- OPRAVA ZDE ---
            // Používáme správný název pole a opravujeme chybu s `it`
            if (prediction.fullMenstruationEstimate.any { it.isAfter(today) || it.isEqual(today) }) {
                // Použijeme `cycleStartDate` místo `prediction.fullMenstruationEstimate.first()`
                val daysUntil = ChronoUnit.DAYS.between(today, prediction.cycleStartDate)
                if (daysUntil >= 0 && !items.any { it.contains("dní do menstruace") }) {
                    when (daysUntil) {
                        0L -> if (!items.any { it.contains("menstruace") }) items.add("Očekávaná menstruace dnes")
                        1L -> items.add("Očekávaná menstruace zítra")
                        else -> items.add("$daysUntil dní do menstruace")
                    }
                }
            }
        }

        if (historicalCycles.isEmpty()) {
            return listOf("Pro zobrazení odhadů zadejte menstruaci.")
        }

        return items.ifEmpty { listOf("Vše v normálu.") }
    }
}