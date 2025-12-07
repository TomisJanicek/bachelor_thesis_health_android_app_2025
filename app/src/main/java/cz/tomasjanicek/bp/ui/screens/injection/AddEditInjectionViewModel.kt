package cz.tomasjanicek.bp.ui.screens.injection

import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.database.injection.IInjectionRepository
import cz.tomasjanicek.bp.model.Injection
import cz.tomasjanicek.bp.model.InjectionCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditInjectionViewModel @Inject constructor(
    private val repository: IInjectionRepository
) : ViewModel(), AddEditInjectionAction {

    private val _uiState = MutableStateFlow<AddEditInjectionUIState>(AddEditInjectionUIState.Loading)
    val uiState: StateFlow<AddEditInjectionUIState> = _uiState.asStateFlow()

    private lateinit var data: AddEditInjectionData

    fun loadInjection(id: Long?) {
        viewModelScope.launch {
            if (id != null && id != 0L && id != -1L) {
                // --- TOTO JE KLÍČOVÁ OPRAVA ---
                // Načteme data z repository a počkáme na první hodnotu z Flow
                val injection = repository.getInjectionById(id).first()
                data = AddEditInjectionData(injection = injection)
                // --- KONEC OPRAVY ---
            } else {
                // Pokud je ID null (nový záznam), vytvoříme prázdná data
                data = createEmptyData()
            }
            _uiState.value = AddEditInjectionUIState.Success(data)
        }
    }

    private fun createEmptyData(): AddEditInjectionData {
        return AddEditInjectionData(
            injection = Injection(
                name = "",
                disease = "",
                category = InjectionCategory.RECOMMENDED,
                date = System.currentTimeMillis()
            )
        )
    }

    override fun onNameChanged(newName: String) {
        data = data.copy(
            injection = data.injection.copy(name = newName),
            nameError = if (newName.isBlank()) R.string.error_field_required else null
        )
        _uiState.value = AddEditInjectionUIState.Success(data)
    }

    override fun onDiseaseChanged(newDisease: String) {
        data = data.copy(
            injection = data.injection.copy(disease = newDisease),
            diseaseError = if (newDisease.isBlank()) R.string.error_field_required else null
        )
        _uiState.value = AddEditInjectionUIState.Success(data)
    }

    override fun onCategoryChanged(newCategory: InjectionCategory) {
        data = data.copy(injection = data.injection.copy(category = newCategory))
        _uiState.value = AddEditInjectionUIState.Success(data)
    }

    override fun onDateChanged(newDate: Long) {
        data = data.copy(injection = data.injection.copy(date = newDate))
        _uiState.value = AddEditInjectionUIState.Success(data)
    }

    override fun onNoteChanged(newNote: String) {
        data = data.copy(injection = data.injection.copy(note = newNote))
        _uiState.value = AddEditInjectionUIState.Success(data)
    }

    override fun saveInjection() {
        // Finální validace před uložením
        onNameChanged(data.injection.name)
        onDiseaseChanged(data.injection.disease)

        viewModelScope.launch {
            val currentData = data // Použijeme aktuální data
            if (currentData.nameError == null && currentData.diseaseError == null) {
                repository.saveInjection(currentData.injection)
                _uiState.value = AddEditInjectionUIState.InjectionSaved
            }
        }
    }

    override fun deleteInjection() {
        viewModelScope.launch {
            if (data.injection.id != 0L) {
                repository.deleteInjection(data.injection.id)
            }
            _uiState.value = AddEditInjectionUIState.InjectionDeleted
        }
    }
}