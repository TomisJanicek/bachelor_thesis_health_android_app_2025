package cz.tomasjanicek.bp.ui.screens.injection

/**
 * Stavy, ve kterých se může obrazovka nacházet.
 */
sealed class AddEditInjectionUIState {
    object Loading : AddEditInjectionUIState()
    data class Success(val data: AddEditInjectionData) : AddEditInjectionUIState()
    object InjectionSaved : AddEditInjectionUIState()
    object InjectionDeleted : AddEditInjectionUIState()
}