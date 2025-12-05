package cz.tomasjanicek.bp.ui.screens.medicine.list

import java.time.LocalDate

sealed class MedicineListAction {
    /** Uživatel zaškrtnul nebo odškrtnul připomínku. */
    data class OnReminderToggled(val reminderId: Long, val isCompleted: Boolean) : MedicineListAction()
    data class OnDeleteMedicineClicked(val medicineId: Long) : MedicineListAction()

    data class OnDateChanged(val newDate: LocalDate) : MedicineListAction()
}