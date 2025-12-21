package cz.tomasjanicek.bp.ui.screens.examination.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationStatus
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import java.util.Calendar



@HiltViewModel
class DetailOfExaminationViewModel @Inject constructor(
    private val examinationRepository: ILocalExaminationsRepository,
    private val doctorRepository: ILocalDoctorsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailOfExaminationUIState>(DetailOfExaminationUIState.Loading)
    val uiState: StateFlow<DetailOfExaminationUIState> = _uiState.asStateFlow()

    private val _allRelatedExaminations = MutableStateFlow<List<Examination>>(emptyList())
    val allRelatedExaminations: StateFlow<List<Examination>> = _allRelatedExaminations.asStateFlow()

    private val _showUpcoming = MutableStateFlow(true)
    val showUpcoming: StateFlow<Boolean> = _showUpcoming.asStateFlow()

    private val _selectedExaminationForDetail = MutableStateFlow<Examination?>(null)
    val selectedExaminationForDetail: StateFlow<Examination?> = _selectedExaminationForDetail.asStateFlow()


    val filteredRelatedExaminations: StateFlow<List<Examination>> =
        combine(_allRelatedExaminations, _showUpcoming) { examinations, upcoming ->
            if (upcoming) {
                // "Naplánované": Vše, co je ve stavu PLANNED (včetně budoucích i prošvihnutých).
                // Seřadíme od nejbližšího termínu.
                examinations
                    .filter { it.status == ExaminationStatus.PLANNED || it.status == ExaminationStatus.OVERDUE }
                    .sortedBy { it.dateTime } // <-- UPRAVENO                    .sortedBy { it.dateTime }
            } else {
                // "Historie": Vše, co je dokončené nebo zrušené.
                // Seřadíme od nejnovějšího (poslední nahoře).
                examinations
                    .filter { it.status == ExaminationStatus.COMPLETED || it.status == ExaminationStatus.CANCELLED }
                    .sortedByDescending { it.dateTime }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Načte detail lékaře a všechny jeho prohlídky.
     * Jako hlavní prohlídku vybere nejbližší plánovanou v budoucnosti.
     */
    fun loadDoctorAndExaminations(doctorId: Long) {
        viewModelScope.launch {
            _uiState.value = DetailOfExaminationUIState.Loading
            try {
                val doctor = doctorRepository.getDoctorWithData(doctorId).first()
                var allExaminations = examinationRepository.getExaminationsByDoctor(doctorId)

                if (doctor == null) {
                    _uiState.value = DetailOfExaminationUIState.Error("Lékař s ID $doctorId nebyl nalezen.")
                    return@launch
                }

                // 2. Najdeme nejbližší BUDOUCÍ prohlídku
                val now = System.currentTimeMillis()
                allExaminations = allExaminations.map { exam ->
                    if (exam.status == ExaminationStatus.PLANNED && exam.dateTime < now) {
                        // Vytvoříme kopii s novým, dočasným stavem OVERDUE
                        exam.copy(status = ExaminationStatus.OVERDUE)
                    } else {
                        exam
                    }
                }
                val mainExamination = allExaminations
                    .filter { it.status == ExaminationStatus.PLANNED && it.dateTime >= now }
                    .minByOrNull { it.dateTime }
                // Pokud žádná budoucí není, vezmeme poslední historickou
                    ?: allExaminations.maxByOrNull { it.dateTime }

                if (mainExamination != null) {
                    // 3. Hlavní prohlídku nastavíme jako "hlavní"
                    val examinationWithDoctor = ExaminationWithDoctor(examination = mainExamination, doctor = doctor)
                    _uiState.value = DetailOfExaminationUIState.Loaded(examinationWithDoctor)

                    // 4. Všechny ostatní prohlídky dáme do seznamu "Další prohlídky"
                    _allRelatedExaminations.value = allExaminations
                } else {
                    // Tento stav nastane, jen když u doktora není VŮBEC ŽÁDNÁ prohlídka
                    _uiState.value = DetailOfExaminationUIState.Error("U tohoto lékaře nejsou žádné záznamy o prohlídkách.")
                }

            } catch (e: Exception) {
                _uiState.value = DetailOfExaminationUIState.Error("Chyba při načítání dat: ${e.message}")
            }
        }
    }

    fun toggleRelatedExaminations(showUpcoming: Boolean) {
        _showUpcoming.value = showUpcoming
    }

    fun showExaminationDetailSheet(examination: Examination) {
        _selectedExaminationForDetail.value = examination
    }

    fun hideExaminationDetailSheet() {
        _selectedExaminationForDetail.value = null
    }

    fun completeExamination(examination: Examination, result: String) {
        viewModelScope.launch {
            // 1. Vytvoříme kopii prohlídky s novým stavem a výsledkem
            val updatedExamination = examination.copy(
                status = ExaminationStatus.COMPLETED,
                result = result
            )
            // 2. Aktualizujeme ji v databázi
            examinationRepository.update(updatedExamination)

            // 3. Bezpečně znovu načteme celou obrazovku, aby se projevily změny
            val currentLoadedState = uiState.value as? DetailOfExaminationUIState.Loaded
            currentLoadedState?.examinationWithDoctor?.doctor?.id?.let { doctorId ->
                loadDoctorAndExaminations(doctorId)
            }

            // 4. Skryjeme modální okno (pokud by bylo otevřené)
            hideExaminationDetailSheet()
        }
    }

    fun deleteExamination(examination: Examination) {
        viewModelScope.launch {
            // 1. Smažeme záznam
            examinationRepository.delete(examination)

            val doctorId = examination.doctorId
            if (doctorId != null) {
                // 2. Místo pouhého načtení (loadDoctorAndExaminations) se podíváme, co zbylo
                val remainingExaminations = examinationRepository.getExaminationsByDoctor(doctorId)

                if (remainingExaminations.isEmpty()) {
                    // 3a. Pokud je seznam prázdný -> přepneme na stav AllDeleted
                    _uiState.value = DetailOfExaminationUIState.AllDeleted
                } else {
                    // 3b. Pokud tam ještě něco je -> načteme obrazovku znovu jako obvykle
                    loadDoctorAndExaminations(doctorId)
                }
            }

            // Skryjeme sheet
            hideExaminationDetailSheet()
        }
    }

    // Funkce, která se volá po kliknutí na "Upravit" v bottom sheetu
// Zde jen skryjeme sheet a navigujeme na editační obrazovku
    fun editExamination(examinationId: Long, navigateAction: (Long) -> Unit) {
        hideExaminationDetailSheet()
        navigateAction(examinationId)
    }

    fun cancelExamination(examination: Examination, reason: String) {
        viewModelScope.launch {        // 1. Vytvoříme kopii prohlídky s novým stavem CANCELLED a důvodem v poli 'result'
            val updatedExamination = examination.copy(
                status = ExaminationStatus.CANCELLED,
                result = reason
            )
            // 2. Aktualizujeme ji v databázi
            examinationRepository.update(updatedExamination)

            // 3. Znovu načteme data, aby se změna projevila v UI
            val doctorId = examination.doctorId
            if (doctorId != null) {
                loadDoctorAndExaminations(doctorId)
            }

            // 4. Skryjeme modální okno
            hideExaminationDetailSheet()
        }
    }
}