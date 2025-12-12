package cz.tomasjanicek.bp.ui.screens.medicine.list

import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import java.time.LocalDate


/**
 * Data potřebná pro vykreslení obrazovky se seznamem léků.
 *
 * @param todaysPlanned Seznam dnešních naplánovaných připomínek.
 * @param todaysCompleted Seznam dnešních již dokončených připomínek.
 * @param medicineDetails Mapa detailů léků (název, dávka atd.) pro rychlé zobrazení. Klíčem je ID léku.
 */
data class MedicineListState(
    val selectedDate: LocalDate = LocalDate.now(), // Přidáno
    val todaysPlanned: List<MedicineReminder> = emptyList(),
    val todaysCompleted: List<MedicineReminder> = emptyList(),
    val medicineDetails: Map<Long, Medicine> = emptyMap(),
    val isLoading: Boolean = true
)