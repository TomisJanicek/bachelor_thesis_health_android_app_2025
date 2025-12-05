package cz.tomasjanicek.bp.ui.screens.medicine.addEdit

import cz.tomasjanicek.bp.model.EndingType
import cz.tomasjanicek.bp.model.MedicineUnit
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Akce, které může uživatel provést na obrazovce pro přidání/úpravu léku.
 */
sealed class AddEditMedicineAction {
    data class OnNameChanged(val name: String) : AddEditMedicineAction()
    data class OnNoteChanged(val note: String) : AddEditMedicineAction()
    data class OnDosageChanged(val dosage: String) : AddEditMedicineAction()
    data class OnUnitSelected(val unit: MedicineUnit) : AddEditMedicineAction()
    data class OnRegularityChanged(val isRegular: Boolean) : AddEditMedicineAction()
    data class OnDayOfWeekToggled(val day: DayOfWeek) : AddEditMedicineAction()
    data class OnStartDateChanged(val dateMillis: Long) : AddEditMedicineAction()
    data class OnTimeAdded(val time: LocalTime) : AddEditMedicineAction()
    data class OnTimeRemoved(val time: LocalTime) : AddEditMedicineAction()
    data class OnSingleDateAdded(val dateTimeMillis: Long) : AddEditMedicineAction()
    data class OnSingleDateRemoved(val dateTimeMillis: Long) : AddEditMedicineAction()

    data class OnEndingTypeChanged(val type: EndingType) : AddEditMedicineAction()
    data class OnEndDateChanged(val dateMillis: Long) : AddEditMedicineAction()
    data class OnDoseCountChanged(val count: String) : AddEditMedicineAction()

    object OnSaveClicked : AddEditMedicineAction()
}