package cz.tomasjanicek.bp.ui.screens.medicine.list

import app.cash.turbine.test
import cz.tomasjanicek.bp.database.medicine.IMedicineRepository
import cz.tomasjanicek.bp.model.EndingType
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.model.MedicineUnit
import cz.tomasjanicek.bp.model.RegularityType
import cz.tomasjanicek.bp.model.ReminderStatus
import cz.tomasjanicek.bp.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class MedicineListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository = mockk<IMedicineRepository>(relaxed = true)
    private lateinit var viewModel: MedicineListViewModel

    // Testovací data
    private val today = LocalDate.now()
    private val tomorrow = today.plusDays(1)

    // --- OPRAVENÁ DEFINICE LÉKU (Žádné TODO) ---
    private val medicine = Medicine(
        id = 100L,
        name = "Ibalgin",
        unit = MedicineUnit.TABLET,
        note = "Na bolest",
        dosage = 1.0,
        isRegular = true,
        regularityType = RegularityType.DAILY,
        regularDays = emptyList(), // Pro DAILY není potřeba
        regularTimes = listOf(480), // 8:00 ráno
        startDate = System.currentTimeMillis(),
        singleDates = null,
        endingType = EndingType.INDEFINITELY,
        endDate = null,
        doseCount = null
    )

    // Připomínka 1: Plánovaná na dnešek
    private val reminderPlanned = MedicineReminder(
        id = 1,
        medicineId = 100L,
        plannedDateTime = System.currentTimeMillis(),
        status = ReminderStatus.PLANNED
    )

    // Připomínka 2: Splněná na dnešek
    private val reminderCompleted = MedicineReminder(
        id = 2,
        medicineId = 100L,
        plannedDateTime = System.currentTimeMillis(),
        status = ReminderStatus.COMPLETED
    )

    @Before
    fun setup() {
        // Defaultní chování mocků (pro dnešní datum)
        // Musíme vrátit Flow, jinak ViewModel zamrzne na combine
        coEvery { mockRepository.getPlannedRemindersForDate(any()) } returns flowOf(emptyList())
        coEvery { mockRepository.getCompletedRemindersForDate(any()) } returns flowOf(emptyList())

        // Mock pro detail léku
        coEvery { mockRepository.getMedicineByIdOnce(100L) } returns medicine

        viewModel = MedicineListViewModel(mockRepository)
    }

    @Test
    fun `initial state loads data correctly`() = runTest {
        // ARRANGE: Nastavíme repozitář, aby vrátil naše data
        coEvery { mockRepository.getPlannedRemindersForDate(today) } returns flowOf(listOf(reminderPlanned))
        coEvery { mockRepository.getCompletedRemindersForDate(today) } returns flowOf(listOf(reminderCompleted))

        // Inicializujeme ViewModel znovu, aby si načetl nové mocky
        viewModel = MedicineListViewModel(mockRepository)

        // ACT & ASSERT
        viewModel.uiState.test {
            // 1. Initial Loading (nebo první emit z combine)
            val state = awaitItem()

            // Pokud jsme chytili Loading, počkáme na další
            val loadedState = if (state.isLoading) awaitItem() else state

            assertFalse(loadedState.isLoading)
            assertEquals(today, loadedState.selectedDate)

            // Kontrola rozdělení seznamů
            assertEquals(1, loadedState.todaysPlanned.size)
            assertEquals(1, loadedState.todaysCompleted.size)

            // Kontrola detailů léku (zda se dotáhl název)
            assertEquals("Ibalgin", loadedState.medicineDetails[100L]?.name)
        }
    }

    @Test
    fun `changing date reloads data from repository`() = runTest {
        // ARRANGE
        // Pro zítřek vrátíme jen prázdné seznamy, abychom poznali rozdíl
        coEvery { mockRepository.getPlannedRemindersForDate(tomorrow) } returns flowOf(emptyList())
        coEvery { mockRepository.getCompletedRemindersForDate(tomorrow) } returns flowOf(emptyList())

        viewModel.uiState.test {
            awaitItem() // Initial/Today state

            // ACT: Změníme datum na zítra
            viewModel.onAction(MedicineListAction.OnDateChanged(tomorrow))

            // ASSERT: Očekáváme nový stav pro zítřek
            val tomorrowState = awaitItem()

            assertEquals(tomorrow, tomorrowState.selectedDate)

            // Ověříme, že se repozitář zeptal na zítřejší datum
            // (formátování data dělá repozitář, my jen ověříme volání metody)
            coVerify { mockRepository.getPlannedRemindersForDate(tomorrow) }
        }
    }

    @Test
    fun `toggling reminder calls repository update`() = runTest {
        // Sledujeme UI jen proto, aby ViewModel běžel
        viewModel.uiState.test {
            awaitItem()

            // ACT: Uživatel odškrtne lék (splněno)
            viewModel.onAction(MedicineListAction.OnReminderToggled(reminderId = 1L, isCompleted = true))

            // ASSERT: Ověříme volání repozitáře
            coVerify(exactly = 1) {
                mockRepository.updateReminderStatus(1L, true)
            }
        }
    }

    @Test
    fun `deleting medicine calls repository delete`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            // ACT: Smazat lék
            viewModel.onAction(MedicineListAction.OnDeleteMedicineClicked(medicineId = 100L))

            // ASSERT
            coVerify(exactly = 1) {
                mockRepository.deleteMedicineAndReminders(100L)
            }
        }
    }
}