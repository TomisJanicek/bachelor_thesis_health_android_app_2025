package cz.tomasjanicek.bp.ui.screens.measurement.addEditCategory


sealed class AddEditCategoryUIState {

    data object Loading : AddEditCategoryUIState()

    data object CategorySaved : AddEditCategoryUIState()

    data object CategoryDeleted : AddEditCategoryUIState()

    data class CategoryChanged(val data: AddEditCategoryData) : AddEditCategoryUIState()
}