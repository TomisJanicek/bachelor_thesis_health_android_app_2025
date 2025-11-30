package cz.tomasjanicek.bp.ui.screens.examination.addEdit

import androidx.annotation.StringRes
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationStatus
import cz.tomasjanicek.bp.model.ExaminationType

/**
 * Datová třída, která drží aktuální stav formuláře pro přidání nebo úpravu prohlídky.
 * Obsahuje veškerá data, která uživatel zadává, a také případné chybové hlášky.
 */
data class AddEditExaminationData(
    var examination: Examination = Examination(
        id = null,
        doctorId = null,
        type = ExaminationType.PROHLIDKA,
        purpose = "",
        note = null,
        result = null,
        dateTime = System.currentTimeMillis(),
        status = ExaminationStatus.PLANNED
    ),

    // Přidáme seznam doktorů, které budeme zobrazovat v dropdown menu
    val doctors: List<Doctor> = emptyList(), // <-- PŘIDÁNO



    // 3. Chybové stavy pro validaci
    //    Používáme @StringRes pro odkaz na textový zdroj (např. R.string.error_field_required),
    //    což je lepší praxe pro vícejazyčnost.

    /** Chyba pro pole "Účel", pokud není vyplněno. */
    @StringRes val purposeError: Int? = null,

    /** Chyba pro pole "Lékař", pokud není vybrán. */
    @StringRes val doctorError: Int? = null,

    /** Chyba pro pole "Datum a čas", pokud není vybráno. */
    @StringRes val dateTimeError: Int? = null
)