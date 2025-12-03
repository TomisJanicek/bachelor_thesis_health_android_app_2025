package cz.tomasjanicek.bp.ui.screens.examination.doctorEdit

/**
 * Reprezentuje stavy obrazovky pro úpravu lékaře.
 */
sealed class DoctorEditUIState {
    /** Stav načítání, když se obrazovka poprvé otevírá. */
    data object Loading : DoctorEditUIState()

    /** Hlavní stav, kdy uživatel interaguje s formulářem. */
    data class Success(val data: DoctorEditData) : DoctorEditUIState()

    /** Stav, když nastane chyba (např. lékař s daným ID nebyl nalezen). */
    data class Error(val message: String) : DoctorEditUIState()

    data object DoctorSaved : DoctorEditUIState()
}