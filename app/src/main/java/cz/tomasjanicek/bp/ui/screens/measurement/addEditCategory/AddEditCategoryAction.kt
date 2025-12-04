package cz.tomasjanicek.bp.ui.screens.measurement.addEditCategory

import cz.tomasjanicek.bp.model.MeasurementCategoryField

interface AddEditCategoryAction {
    fun saveCategory()
    fun deleteCategory()

    fun onNameChanged(name: String)
    fun onDescriptionChanged(description: String)

    // --- Nové akce pro dialog ---
    fun onParameterDialogOpened(field: MeasurementCategoryField?) // null pro nový, objekt pro editaci
    fun onParameterDialogDismissed()
    fun onParameterSaved()
    fun onParameterFieldChanged(label: String, unit: String, min: String, max: String)
    fun onParameterDeleted(field: MeasurementCategoryField)
}