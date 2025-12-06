package cz.tomasjanicek.bp.ui.screens.stats

import java.time.LocalDate

/**
 * Akce, které může uživatel provést na obrazovce se statistikami.
 */
sealed class StatsAction {
    /** Uživatel změnil předdefinované období (Den, Týden, ...). */
    data class OnPeriodTypeChanged(val periodType: StatsPeriodType) : StatsAction()

    /** Uživatel změnil počáteční datum ve vlastním rozsahu. */
    data class OnCustomStartDateChanged(val date: LocalDate) : StatsAction()

    /** Uživatel změnil koncové datum ve vlastním rozsahu. */
    data class OnCustomEndDateChanged(val date: LocalDate) : StatsAction()

    /** Uživatel zaškrtnul nebo odškrtnul kategorii měření pro zobrazení. */
    data class OnCategorySelectionChanged(val categoryId: Long, val isSelected: Boolean) : StatsAction()

    /** Uživatel kliknul na tlačítko pro export do PDF. */
    object OnExportClicked : StatsAction()
}