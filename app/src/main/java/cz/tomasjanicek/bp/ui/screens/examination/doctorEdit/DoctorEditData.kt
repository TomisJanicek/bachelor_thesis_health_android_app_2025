package cz.tomasjanicek.bp.ui.screens.examination.doctorEdit

import androidx.annotation.StringRes
import cz.tomasjanicek.bp.model.Doctor

/**
 * Datová třída, která drží aktuální stav formuláře pro úpravu lékaře.
 */
data class DoctorEditData(
    // Pole `doctor` už nemůže mít výchozí hodnotu, protože vždy upravujeme existujícího.
    // Může být `null` pouze během načítání.
    val doctor: Doctor? = null,

    // Chybové stavy pro validaci (jméno stále potřebuje validaci)
    @StringRes val nameError: Int? = null,
)