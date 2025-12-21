package cz.tomasjanicek.bp.ui.screens.measurement.defaultCategories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.model.data.MeasurementData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Pomocná třída
data class SelectableCategory(
    val definition: MeasurementData.CategoryDef,
    val isSelected: Boolean
)

@HiltViewModel
class DefaultCategoriesViewModel @Inject constructor(
    private val categoryRepository: ILocalMeasurementCategoriesRepository
) : ViewModel() {

    // Defaultně nic nevybráno
    private val _categories = MutableStateFlow(
        MeasurementData.defaultCategories.map {
            SelectableCategory(it, isSelected = false)
        }
    )
    val categories = _categories.asStateFlow()

    fun toggleSelection(categoryName: String) {
        _categories.update { currentList ->
            currentList.map { item ->
                if (item.definition.name == categoryName) {
                    item.copy(isSelected = !item.isSelected)
                } else {
                    item
                }
            }
        }
    }

    fun saveSelectedCategories(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val selectedDefs = _categories.value
                .filter { it.isSelected }
                .map { it.definition }

            if (selectedDefs.isNotEmpty()) {
                categoryRepository.createSelectedDefaultCategories(selectedDefs)
            }
            onSuccess()
        }
    }
}