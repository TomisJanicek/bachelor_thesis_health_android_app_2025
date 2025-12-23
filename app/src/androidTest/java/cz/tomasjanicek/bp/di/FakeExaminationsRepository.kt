package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationStatus
import cz.tomasjanicek.bp.model.ExaminationType
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FakeExaminationsRepository @Inject constructor() : ILocalExaminationsRepository {

    // Pomocná metoda pro simulaci Flow emise
    // V reálu by se použil MutableStateFlow, ale pro jednoduchý test stačí takto,
    // pokud ViewModel reaguje na změny (což u FlowOf nemusí vždy fungovat na 100% dynamicky).
    // Pro robustnější testy by FakeDatabase měla mít StateFlows.
    // Ale zkusme to jednoduše:

    private val _examinationsFlow = MutableStateFlow<List<Examination>>(emptyList())

    // Inicializace flow daty z FakeDatabase
    private fun refreshFlow() {
        _examinationsFlow.value = FakeDatabase.examinations.toList()
    }

    override fun getAll(): Flow<List<Examination>> {
        refreshFlow()
        return _examinationsFlow
    }

    override fun getAllWithDoctors(): Flow<List<ExaminationWithDoctor>> {
        refreshFlow()
        return _examinationsFlow.map { exams ->
            exams.map { exam ->
                // TADY JE KLÍČ: Hledáme doktora ve společné FakeDatabase
                val doc = FakeDatabase.doctors.find { it.id == exam.doctorId }
                ExaminationWithDoctor(exam, doc)
            }
        }
    }

    override suspend fun insert(examination: Examination): Long {
        val currentList = FakeDatabase.examinations
        val newId = if ((examination.id ?: 0) == 0L) (currentList.maxOfOrNull { it.id ?: 0 } ?: 0) + 1 else examination.id!!
        val newExam = examination.copy(id = newId)

        currentList.add(newExam)
        refreshFlow() // Aktualizujeme Flow
        return newId
    }

    override suspend fun update(examination: Examination) {
        val index = FakeDatabase.examinations.indexOfFirst { it.id == examination.id }
        if (index != -1) {
            FakeDatabase.examinations[index] = examination
            refreshFlow()
        }
    }

    override suspend fun delete(examination: Examination) {
        FakeDatabase.examinations.removeIf { it.id == examination.id }
        refreshFlow()
    }

    override suspend fun getExamination(id: Long): Examination {
        return FakeDatabase.examinations.first { it.id == id }
    }

    override suspend fun getExaminationsByDoctor(doctorId: Long): List<Examination> {
        return FakeDatabase.examinations.filter { it.doctorId == doctorId }
    }

    override fun getExaminationWithDoctorById(id: Long): Flow<ExaminationWithDoctor?> {
        refreshFlow()
        return _examinationsFlow.map { list ->
            val exam = list.find { it.id == id }
            if (exam != null) {
                // Opět hledáme doktora ve sdílené DB
                val doc = FakeDatabase.doctors.find { it.id == exam.doctorId }
                ExaminationWithDoctor(exam, doc)
            } else {
                null
            }
        }
    }
}