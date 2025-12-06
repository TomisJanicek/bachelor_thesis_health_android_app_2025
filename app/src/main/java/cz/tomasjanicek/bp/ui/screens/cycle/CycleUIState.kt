package cz.tomasjanicek.bp.ui.screens.cycle

import cz.tomasjanicek.bp.model.CycleRecord
import java.time.LocalDate
import java.time.YearMonth

/**
* Definuje, jaký typ má konkrétní den v kalendáři.
*/
enum class DayType {
    NORMAL,
    MENSTRUATION,
    FERTILE,
    OVULATION,
    PREDICTED_MENSTRUATION,
    PREDICTED_FERTILE,
    PREDICTED_OVULATION
}

/**
 * Reprezentuje jeden den v kalendáři pro UI.
 */
data class CalendarDay(
    val day: Int,
    val type: DayType,
    val isToday: Boolean
)

/**
 * Informace pro zobrazení dialogového okna pro úpravu dne.
 * @param date Datum, kterého se dialog týká.
 * @param hasExistingRecord True, pokud na tomto dni již existuje nějaký záznam.
 */
data class EditDialogInfo(
    val date: LocalDate,
    val hasExistingRecord: Boolean
)

/**
 * Kompletní stav pro obrazovku sledování cyklu.
 */
data class CycleUIState(
    val isLoading: Boolean = true,
    val selectedMonth: YearMonth = YearMonth.now(),
    val calendarDays: List<CalendarDay> = emptyList(),
    val showEditDialog: EditDialogInfo? = null,
    val averageCycleLength: Long = 0,
    val averageMenstruationLength: Long = 0,
    val statusCarouselItems: List<String> = emptyList()
)