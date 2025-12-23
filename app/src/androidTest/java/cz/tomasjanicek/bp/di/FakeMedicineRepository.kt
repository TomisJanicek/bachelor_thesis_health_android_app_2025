package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.medicine.IMedicineRepository
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.model.ReminderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class FakeMedicineRepository @Inject constructor() : IMedicineRepository {

    // Pomocná funkce pro refresh
    private val _remindersFlow = MutableStateFlow<List<MedicineReminder>>(emptyList())

    private fun refresh() {
        _remindersFlow.value = FakeDatabase.reminders.toList()
    }

    // Helper pro převod Long (timestamp) na LocalDate
    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    override fun getPlannedRemindersForDate(date: LocalDate): Flow<List<MedicineReminder>> {
        refresh()
        return _remindersFlow.map { list ->
            list.filter {
                // Filtrujeme podle data a statusu PLANNED
                it.plannedDateTime.toLocalDate() == date && it.status == ReminderStatus.PLANNED
            }
        }
    }

    override fun getCompletedRemindersForDate(date: LocalDate): Flow<List<MedicineReminder>> {
        refresh()
        return _remindersFlow.map { list ->
            list.filter {
                // Filtrujeme podle data (kdy se to mělo stát, nebo kdy se to stalo) a statusu COMPLETED
                val comparisonDate = it.completionDateTime?.toLocalDate() ?: it.plannedDateTime.toLocalDate()
                comparisonDate == date && it.status == ReminderStatus.COMPLETED
            }
        }
    }

    override fun getTodaysPlannedReminders(): Flow<List<MedicineReminder>> = getPlannedRemindersForDate(LocalDate.now())
    override fun getTodaysCompletedReminders(): Flow<List<MedicineReminder>> = getCompletedRemindersForDate(LocalDate.now())

    override fun getMedicineById(id: Long): Flow<Medicine?> {
        return flowOf(FakeDatabase.medicines.find { it.id == id })
    }

    override suspend fun getMedicineByIdOnce(id: Long): Medicine? {
        return FakeDatabase.medicines.find { it.id == id }
    }

    override suspend fun saveMedicineAndGenerateReminders(medicine: Medicine) {
        val newId = if (medicine.id == 0L) (FakeDatabase.medicines.maxOfOrNull { it.id } ?: 0) + 1 else medicine.id
        val newMed = medicine.copy(id = newId)

        // Update nebo Insert
        FakeDatabase.medicines.removeIf { it.id == newId }
        FakeDatabase.medicines.add(newMed)

        // Vygenerujeme dummy reminder pro test (vždy na dnešek 8:00)
        // V reálném testu si to pravděpodobně vložíte manuálně, ale pro úplnost:
        val today8am = LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val reminder = MedicineReminder(
            id = (FakeDatabase.reminders.maxOfOrNull { it.id } ?: 0) + 1,
            medicineId = newId,
            plannedDateTime = today8am,
            status = ReminderStatus.PLANNED,
            completionDateTime = null
        )
        FakeDatabase.reminders.add(reminder)
        refresh()
    }

    // --- Helper metody pro testy ---

    suspend fun insertTestMedicine(medicine: Medicine) {
        FakeDatabase.medicines.add(medicine)
    }

    suspend fun insertTestReminder(reminder: MedicineReminder) {
        FakeDatabase.reminders.add(reminder)
        refresh()
    }

    override suspend fun updateReminderStatus(reminderId: Long, isCompleted: Boolean) {
        val index = FakeDatabase.reminders.indexOfFirst { it.id == reminderId }
        if (index != -1) {
            val old = FakeDatabase.reminders[index]

            val newStatus = if (isCompleted) ReminderStatus.COMPLETED else ReminderStatus.PLANNED
            val completionTime = if (isCompleted) System.currentTimeMillis() else null

            FakeDatabase.reminders[index] = old.copy(
                status = newStatus,
                completionDateTime = completionTime
            )
            refresh()
        }
    }

    override suspend fun deleteMedicineAndReminders(medicineId: Long) {
        FakeDatabase.medicines.removeIf { it.id == medicineId }
        FakeDatabase.reminders.removeIf { it.medicineId == medicineId }
        refresh()
    }
}