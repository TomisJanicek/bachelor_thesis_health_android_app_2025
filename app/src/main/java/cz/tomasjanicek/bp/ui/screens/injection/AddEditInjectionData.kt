package cz.tomasjanicek.bp.ui.screens.injection

import cz.tomasjanicek.bp.model.Injection

/**
 * Datová třída pro držení stavu formuláře.
 */
data class AddEditInjectionData(
    val injection: Injection,
    val nameError: Int? = null,
    val diseaseError: Int? = null,
    val dateError: Int? = null,
)