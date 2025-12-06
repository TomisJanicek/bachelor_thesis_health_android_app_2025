package cz.tomasjanicek.bp.ui.screens.cycle

import java.time.LocalDate

/**
 * Akce, které může uživatel na obrazovce cyklu provést.
 */
sealed class CycleAction {
    data class ShowEditDialog(val date: LocalDate) : CycleAction()
    object DismissEditDialog : CycleAction()

    // Nové, jednoduché akce
    data class LogMenstruation(val date: LocalDate) : CycleAction()
    data class LogOvulation(val date: LocalDate) : CycleAction()
    data class DeleteEvent(val date: LocalDate) : CycleAction()

    object PreviousMonthClicked : CycleAction()
    object NextMonthClicked : CycleAction()
}