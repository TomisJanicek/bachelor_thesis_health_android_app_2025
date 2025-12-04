package cz.tomasjanicek.bp.ui.screens.measurement.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListOfMeasurementViewModel @Inject constructor(
    private val categoriesRepository: ILocalMeasurementCategoriesRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<ListOfMeasurementUIState> =
        MutableStateFlow(ListOfMeasurementUIState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoriesRepository
                .getAllCategoriesWithFields()
                .collect { list: List<MeasurementCategoryWithFields> ->
                    _uiState.value = ListOfMeasurementUIState.Content(list)
                }
        }
    }
}