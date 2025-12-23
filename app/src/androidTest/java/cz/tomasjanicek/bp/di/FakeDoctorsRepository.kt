package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.model.Doctor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeDoctorsRepository @Inject constructor() : ILocalDoctorsRepository {

    // Nyní čteme ze sdílené "databáze"
    private fun getDoctorsFlow() = flowOf(FakeDatabase.doctors.toList())

    override fun getAll(): Flow<List<Doctor>> = getDoctorsFlow()

    override fun getDoctorWithData(id: Long): Flow<Doctor> {
        // Najdeme doktora v seznamu, nebo vrátíme prázdného (aby nepadal flow)
        val doc = FakeDatabase.doctors.find { it.id == id } ?: Doctor(id = -1, specialization = "", name = "Neznámý")
        return flowOf(doc)
    }

    override suspend fun insert(doctor: Doctor): Long {
        // Simulace autogenerate ID, pokud je 0
        val idToSave = if (doctor.id == 0L) (FakeDatabase.doctors.maxOfOrNull { it.id } ?: 0) + 1 else doctor.id
        val newDoctor = doctor.copy(id = idToSave)

        FakeDatabase.doctors.removeIf { it.id == idToSave } // Update pokud existuje
        FakeDatabase.doctors.add(newDoctor)

        return idToSave
    }

    override suspend fun update(doctor: Doctor) {
        insert(doctor)
    }

    override suspend fun delete(doctor: Doctor) {
        FakeDatabase.doctors.removeIf { it.id == doctor.id }
    }

    override suspend fun deleteAll() {
        FakeDatabase.doctors.clear()
    }

    override suspend fun resetDoctors() {
        FakeDatabase.doctors.clear()
    }

    override suspend fun initializeDoctorsIfEmpty() {}

    override suspend fun getDoctor(id: Long?): Doctor? {
        return FakeDatabase.doctors.find { it.id == id }
    }
}