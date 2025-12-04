package cz.tomasjanicek.bp.ui.screens.measurement.addEditCategory

import androidx.compose.animation.core.copy
import androidx.compose.foundation.gestures.forEach
import androidx.compose.ui.test.cancel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.utils.toSnakeCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditCategoryViewModel @Inject constructor(
    private val categoriesRepository: ILocalMeasurementCategoriesRepository
) : ViewModel(), AddEditCategoryAction {

    private val _uiState = MutableStateFlow<AddEditCategoryUIState>(AddEditCategoryUIState.Loading)
    val uiState = _uiState.asStateFlow()

    private val categoryIdFlow = MutableStateFlow<Long?>(null)
    private var dataSubscriptionJob: Job? = null

    fun loadCategory(id: Long?) {
        if (categoryIdFlow.value == id && _uiState.value !is AddEditCategoryUIState.Loading) return

        categoryIdFlow.value = id
        dataSubscriptionJob?.cancel()

        if (id == null) {
            _uiState.value = AddEditCategoryUIState.CategoryChanged(AddEditCategoryData())
        } else {
            dataSubscriptionJob = categoriesRepository.getCategoryWithFieldsById(id)
                .distinctUntilChanged()
                .onEach { categoryWithFields ->
                    val data = if (categoryWithFields != null) {
                        AddEditCategoryData(
                            category = categoryWithFields.category,
                            fields = categoryWithFields.fields
                        )
                    } else {
                        AddEditCategoryData()
                    }
                    _uiState.value = AddEditCategoryUIState.CategoryChanged(data)
                }
                .launchIn(viewModelScope)
        }
    }

    private fun updateState(block: (AddEditCategoryData) -> AddEditCategoryData) {
        _uiState.update { current ->
            if (current is AddEditCategoryUIState.CategoryChanged) {
                current.copy(data = block(current.data))
            } else current
        }
    }

    override fun saveCategory() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is AddEditCategoryUIState.CategoryChanged) return@launch

            val data = currentState.data
            var isValid = true

            // Validace názvu kategorie
            val nameError = if (data.category.name.isBlank()) {
                isValid = false
                R.string.error_field_required
            } else null

            // Validace: alespoň jeden parametr
            val fieldsError = if (data.fields.isEmpty()) {
                isValid = false
                R.string.error_field_required //TODO dát jiný
            } else null

            if (!isValid) {
                updateState { it.copy(nameError = nameError, fieldsError = fieldsError) }
                return@launch
            }

            val categoryId: Long = if (data.category.id == 0L) {
                categoriesRepository.insertCategory(data.category)
            } else {
                categoriesRepository.updateCategory(data.category)
                data.category.id
            }

            val existingFieldIds = categoriesRepository.getFieldsForCategory(categoryId)
                .firstOrNull()?.map { it.id } ?: emptyList()
            val currentFieldIds = data.fields.map { it.id }

            existingFieldIds.filterNot { it in currentFieldIds }.forEach { fieldIdToRemove ->
                categoriesRepository.deleteField(MeasurementCategoryField(id = fieldIdToRemove, categoryId = categoryId, name = "", label = ""))
            }

            val fieldsToSave = data.fields.map { it.copy(categoryId = categoryId) }
            categoriesRepository.insertFields(fieldsToSave)

            _uiState.value = AddEditCategoryUIState.CategorySaved
        }
    }

    override fun deleteCategory() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is AddEditCategoryUIState.CategoryChanged && currentState.data.category.id != 0L) {
                categoriesRepository.deleteCategory(currentState.data.category)
            }
            _uiState.value = AddEditCategoryUIState.CategoryDeleted
        }
    }

    override fun onNameChanged(name: String) {
        updateState { it.copy(category = it.category.copy(name = name), nameError = null) }
    }

    override fun onDescriptionChanged(description: String) {
        updateState { it.copy(category = it.category.copy(description = description.ifBlank { null })) }
    }

    // --- LOGIKA PRO DIALOG ---

    override fun onParameterDialogOpened(field: MeasurementCategoryField?) {
        updateState {
            it.copy(
                editingField = field ?: MeasurementCategoryField(
                    id = ((it.fields.map { f -> f.id }.filter { id -> id < 0 }.minOrNull() ?: 0L) - 1L), // Nové dočasné ID
                    categoryId = it.category.id,
                    name = "",
                    label = ""
                ),
                isEditingParameter = true
            )
        }
    }

    override fun onParameterDialogDismissed() {
        updateState { it.copy(isEditingParameter = false, editingField = null, editingFieldError = null) }
    }

    override fun onParameterSaved() {
        updateState { data ->
            val editingField = data.editingField ?: return@updateState data
            // Validace
            if (editingField.label.isBlank() || editingField.name.isBlank()) {
                return@updateState data.copy(editingFieldError = R.string.error_field_required) //TODO upravit
            }

            val existingFieldIndex = data.fields.indexOfFirst { it.id == editingField.id }
            val newFields = if (existingFieldIndex != -1) {
                // Aktualizace existujícího
                data.fields.toMutableList().apply { set(existingFieldIndex, editingField) }
            } else {
                // Přidání nového
                data.fields + editingField
            }
            data.copy(
                fields = newFields,
                isEditingParameter = false,
                editingField = null,
                editingFieldError = null,
                fieldsError = null // Smažeme chybu o prázdném seznamu, když přidáme první
            )
        }
    }

    override fun onParameterFieldChanged(label: String, unit: String, min: String, max: String) {
        updateState { data ->
            val newName = label.toSnakeCase()

            // Převedeme text na nullable Double, nahradíme čárku tečkou pro správný formát
            val minValue = min.replace(',', '.').toDoubleOrNull()
            val maxValue = max.replace(',', '.').toDoubleOrNull()

            data.copy(
                editingField = data.editingField?.copy(
                    label = label,
                    name = newName,
                    unit = unit.ifBlank { null },
                    // --- PŘIŘAZENÍ NOVÝCH HODNOT ---
                    minValue = minValue,
                    maxValue = maxValue
                ),
                editingFieldError = null
            )
        }
    }

    override fun onParameterDeleted(field: MeasurementCategoryField) {
        updateState { it.copy(fields = it.fields.filterNot { f -> f.id == field.id }) }
    }
}