package cz.tomasjanicek.bp.ui.screens.medicine.addEdit

import cz.tomasjanicek.bp.model.EndingType
import cz.tomasjanicek.bp.model.MedicineUnit
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Reprezentuje stav UI obrazovky pro přidání/úpravu léku.
 *
 * @param isLoading Zda se načítají data pro úpravu existujícího léku.
 * @param isEditing Zda se jedná o úpravu (true) nebo přidání nového léku (false).
 * @param medicineId ID upravovaného léku.
 * @param name Název léku.
 * @param note Poznámka.
 * @param dosage Dávka jako textový řetězec pro validaci.
 * @param unit Zvolená jednotka.
 * @param isRegular Zda je lék pravidelný.
 * @param regularDays Vybrané dny v týdnu pro pravidelné užívání.
 * @param regularTimes Vybrané časy pro pravidelné užívání.
 * @param startDate Počáteční datum pro pravidelné užívání (v ms).
 * @param singleDates Seznam konkrétních časů pro jednorázové užívání.
 * @param canBeSaved Zda jsou data validní a je možné lék uložit.
 */
data class AddEditMedicineUIState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val medicineId: Long? = null,

    val name: String = "",
    val note: String = "",
    val dosage: String = "",
    val unit: MedicineUnit = MedicineUnit.TABLET,
    val isRegular: Boolean = true,

    // Pravidelné užívání
    val regularDays: Set<DayOfWeek> = emptySet(),
    val regularTimes: Set<LocalTime> = emptySet(),
    val startDate: Long = System.currentTimeMillis(),

    // Jednorázové užívání
    val singleDates: Set<Long> = emptySet(),

    // NOVÉ VLASTNOSTI
    /** Reprezentuje, zda se uživatel už pokusil uložit nevalidní formulář. */
    val hasAttemptedSave: Boolean = false,

    /** Typ ukončení pravidelného užívání. */
    val endingType: EndingType = EndingType.INDEFINITELY,

    /** Koncové datum pro typ 'UNTIL_DATE'. */
    val endDate: Long? = null,

    /** Počet dávek pro typ 'AFTER_DOSES'. */
    val doseCount: String = "",

    // ... canBeSaved zůstává ...
    val canBeSaved: Boolean = false
)