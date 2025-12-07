package cz.tomasjanicek.bp.ui.screens.injection

import cz.tomasjanicek.bp.model.InjectionCategory

/**
 * Akce, které může uživatel provést.
 */
interface AddEditInjectionAction {
    fun onNameChanged(newName: String)
    fun onDiseaseChanged(newDisease: String)
    fun onCategoryChanged(newCategory: InjectionCategory)
    fun onDateChanged(newDate: Long)
    fun onNoteChanged(newNote: String)
    fun saveInjection()
    fun deleteInjection()
}