package cz.tomasjanicek.bp.ui.screens.measurement.addEditCategory

import androidx.annotation.StringRes
import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField

data class AddEditCategoryData(
    val category: MeasurementCategory = MeasurementCategory(id = 0, name = "", description = null),
    val fields: List<MeasurementCategoryField> = emptyList(),

    // --- Stavy pro dialog ---
    val isEditingParameter: Boolean = false,
    val editingField: MeasurementCategoryField? = null,
    @StringRes val editingFieldError: Int? = null,

    // --- Chyby hlavního formuláře ---
    @StringRes val nameError: Int? = null,
    @StringRes val fieldsError: Int? = null,
)