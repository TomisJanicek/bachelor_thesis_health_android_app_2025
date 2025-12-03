package cz.tomasjanicek.bp.ui.screens.examination.mapSelector

import com.google.android.gms.maps.model.LatLng

/**
 * Reprezentuje kompletní stav obrazovky pro výběr polohy na mapě.
 */
data class MapSelectorState(
    /** Příznak, zda se právě načítá aktuální poloha. */
    val isLoading: Boolean = true,
    /** Počáteční/aktuální poloha uživatele. */
    val userPosition: LatLng? = null,
    /** Poloha vybraná uživatelem kliknutím na mapu. */
    val selectedPosition: LatLng? = null,
    /** Pozice kamery, kterou ovládá uživatel. */
    val initialCameraPosition: LatLng? = null
)