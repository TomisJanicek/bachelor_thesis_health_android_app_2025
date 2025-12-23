package cz.tomasjanicek.bp.ui.screens.medicine.addEdit

import app.cash.turbine.test
import cz.tomasjanicek.bp.database.medicine.IMedicineRepository
import cz.tomasjanicek.bp.model.EndingType
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineUnit
import cz.tomasjanicek.bp.model.RegularityType
import cz.tomasjanicek.bp.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime

class AddEditMedicineViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository = mockk<IMedicineRepository>(relaxed = true)
    private lateinit var viewModel: AddEditMedicineViewModel

    private val dummyMedicine = Medicine(
        id = 100L,
        name = "Paralen",
        isRegular = true,
        regularityType = RegularityType.DAILY,
        dosage = 1.0,
        unit = MedicineUnit.TABLET,
        startDate = 1000L,
        regularTimes = listOf(480), // 8:00
        note = "poznamka"
    )

    @Before
    fun setup() {
        coEvery { mockRepository.getMedicineById(100L) } returns flowOf(dummyMedicine)
        viewModel = AddEditMedicineViewModel(mockRepository)
    }

    @Test
    fun `loadMedicine with null ID initializes empty form`() = runTest {
        viewModel.loadMedicine(null)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("", state.name)
        }
    }

    @Test
    fun `loadMedicine with valid ID loads data correctly`() = runTest {
        viewModel.loadMedicine(100L)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.name.isEmpty()) { state = awaitItem() }
            assertEquals("Paralen", state.name)
        }
    }

    @Test
    fun `validation logic - cannot save empty form`() = runTest {
        viewModel.loadMedicine(null)
        viewModel.uiState.test {
            awaitItem() // Skip initial

            viewModel.onAction(AddEditMedicineAction.OnSaveClicked)
            val state = awaitItem() // State after click

            assertTrue(state.hasAttemptedSave)
            assertFalse(state.canBeSaved)
            coVerify(exactly = 0) { mockRepository.saveMedicineAndGenerateReminders(any()) }
        }
    }

    @Test
    fun `validation logic - regular medicine needs times`() = runTest {
        viewModel.loadMedicine(null)
        viewModel.uiState.test {
            awaitItem()

            // 1. Změna jména -> awaitItem
            viewModel.onAction(AddEditMedicineAction.OnNameChanged("Ibalgin"))
            awaitItem()

            // 2. Změna dávky -> awaitItem
            viewModel.onAction(AddEditMedicineAction.OnDosageChanged("1"))
            awaitItem()

            // 3. Pokus o uložení -> awaitItem
            viewModel.onAction(AddEditMedicineAction.OnSaveClicked)
            val state = awaitItem()

            assertFalse("Should be invalid because no time is selected", state.canBeSaved)
        }
    }

    @Test
    fun `saveMedicine - happy path for Regular Medicine`() = runTest {
        viewModel.loadMedicine(null)
        viewModel.uiState.test {
            awaitItem()

            // 1. Jméno -> await
            viewModel.onAction(AddEditMedicineAction.OnNameChanged("Vitamín C"))
            awaitItem()

            // 2. Dávka -> await
            viewModel.onAction(AddEditMedicineAction.OnDosageChanged("1"))
            awaitItem()

            // 3. Čas -> await
            viewModel.onAction(AddEditMedicineAction.OnTimeAdded(LocalTime.of(10, 0)))
            awaitItem()

            // 4. Uložit -> await (validace)
            viewModel.onAction(AddEditMedicineAction.OnSaveClicked)
            awaitItem()

            // 5. Ověříme volání repozitáře
            coVerify(exactly = 1) {
                mockRepository.saveMedicineAndGenerateReminders(match {
                    it.name == "Vitamín C" && it.regularTimes.contains(600)
                })
            }
        }
    }

    @Test
    fun `saveMedicine - happy path for Single-Use Medicine`() = runTest {
        viewModel.loadMedicine(null)
        viewModel.uiState.test {
            awaitItem()

            // 1. Přepnutí -> await
            viewModel.onAction(AddEditMedicineAction.OnRegularityChanged(false))
            awaitItem()

            // 2. Jméno -> await
            viewModel.onAction(AddEditMedicineAction.OnNameChanged("Injekce"))
            awaitItem()

            // 3. Dávka -> await
            viewModel.onAction(AddEditMedicineAction.OnDosageChanged("1"))
            awaitItem()

            // 4. Datum -> await
            val dateMillis = 1700000000000L
            viewModel.onAction(AddEditMedicineAction.OnSingleDateAdded(dateMillis))
            awaitItem()

            // 5. Uložit -> await
            viewModel.onAction(AddEditMedicineAction.OnSaveClicked)
            awaitItem()

            // 6. Verify
            coVerify(exactly = 1) {
                mockRepository.saveMedicineAndGenerateReminders(match {
                    !it.isRegular && it.singleDates?.contains(dateMillis) == true
                })
            }
        }
    }

    @Test
    fun `validation - EndingType AFTER_DOSES requires count`() = runTest {
        viewModel.loadMedicine(null)
        viewModel.uiState.test {
            awaitItem()

            // 1. Nastavíme typ ukončení a vyplníme povinná pole
            viewModel.onAction(AddEditMedicineAction.OnEndingTypeChanged(EndingType.AFTER_DOSES)); awaitItem()
            viewModel.onAction(AddEditMedicineAction.OnNameChanged("Lék")); awaitItem()
            viewModel.onAction(AddEditMedicineAction.OnDosageChanged("1")); awaitItem()

            // Přidáme čas -> to vyvolá změnu stavu
            viewModel.onAction(AddEditMedicineAction.OnTimeAdded(LocalTime.NOON))
            var state = awaitItem()

            // 2. KONTROLA: Zde musí být validace FALSE, protože 'doseCount' je stále prázdný (default)
            // Nemusíme volat onDoseCountChanged(""), protože to by nevyvolalo změnu.
            assertFalse("Should be invalid because dose count is missing", state.canBeSaved)

            // 3. Zadáme validní počet
            viewModel.onAction(AddEditMedicineAction.OnDoseCountChanged("10"))
            state = awaitItem() // Tady už se stav změní ("" -> "10"), takže awaitItem projde

            // 4. KONTROLA: Teď už musí být validní
            assertTrue("Should be valid now", state.canBeSaved)
        }
    }
}