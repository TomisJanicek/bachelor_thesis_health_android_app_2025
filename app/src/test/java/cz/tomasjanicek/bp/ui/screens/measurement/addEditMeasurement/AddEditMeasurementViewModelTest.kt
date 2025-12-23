package cz.tomasjanicek.bp.ui.screens.measurement.addEditMeasurement

import app.cash.turbine.test
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementsRepository
import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import cz.tomasjanicek.bp.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditMeasurementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockMeasurementRepository = mockk<ILocalMeasurementsRepository>(relaxed = true)
    private val mockCategoryRepository = mockk<ILocalMeasurementCategoriesRepository>()

    private lateinit var viewModel: AddEditMeasurementViewModel

    // Test data
    private val dummyCategory = MeasurementCategory(id = 1, name = "Váha")
    private val dummyField = MeasurementCategoryField(id = 10, categoryId = 1, name = "weight", label = "Hmotnost")
    private val dummyCategoryWithFields = MeasurementCategoryWithFields(dummyCategory, listOf(dummyField))

    @Before
    fun setup() {
        // Mock pro kategorii (vždy musí vrátit něco, jinak ViewModel zůstane v Loading)
        coEvery { mockCategoryRepository.getCategoryWithFieldsById(1L) } returns flowOf(dummyCategoryWithFields)
        // Mock pro měření (vrací null = nové měření)
        coEvery { mockMeasurementRepository.getMeasurementWithValuesById(-1L) } returns flowOf(null)

        viewModel = AddEditMeasurementViewModel(mockMeasurementRepository, mockCategoryRepository)
    }

    @Test
    fun `loadMeasurement initializes new measurement correctly`() = runTest {
        // 1. Spustíme načítání (nastaví ID do flow)
        viewModel.loadMeasurement(categoryId = 1L, measurementId = null)

        viewModel.uiState.test {
            // 2. První stav může být Loading
            val firstItem = awaitItem()

            val loadedState = if (firstItem is AddEditMeasurementUIState.Loading) {
                awaitItem() as AddEditMeasurementUIState.MeasurementChanged
            } else {
                firstItem as AddEditMeasurementUIState.MeasurementChanged
            }

            assertEquals(0L, loadedState.data.measurement.id)

            // --- OPRAVA ZDE (Přidán otazník ?.name) ---
            assertEquals("Váha", loadedState.data.category?.name)

            // Ověříme, že se vygenerovalo UI pro pole "Hmotnost"
            assertEquals(1, loadedState.data.fields.size)
            assertEquals("Hmotnost", loadedState.data.fields[0].field.label)
        }
    }

    @Test
    fun `saveMeasurement with invalid value shows error`() = runTest {
        viewModel.loadMeasurement(1L, null)

        viewModel.uiState.test {
            // Skip loading
            var state = awaitItem()
            while (state !is AddEditMeasurementUIState.MeasurementChanged) { state = awaitItem() }

            // 1. Zadáme neplatnou hodnotu (text místo čísla)
            viewModel.onFieldValueChanged(10L, "invalid")
            // State update (hodnota v UI se změnila)
            awaitItem()

            // 2. Uložit
            viewModel.saveMeasurement()

            // 3. Očekáváme update stavu s chybou
            val errorState = awaitItem() as AddEditMeasurementUIState.MeasurementChanged
            assertEquals(R.string.error_field_type, errorState.data.fields[0].error)

            // Repozitář se nevolá
            coVerify(exactly = 0) { mockMeasurementRepository.insertMeasurementWithValues(any(), any()) }
        }
    }

    @Test
    fun `saveMeasurement with valid value calls repository and navigates back`() = runTest {
        viewModel.loadMeasurement(1L, null)

        // Sledujeme UI State i Eventy (navigace)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is AddEditMeasurementUIState.MeasurementChanged) { state = awaitItem() }

            // 1. Zadáme platnou hodnotu
            viewModel.onFieldValueChanged(10L, "85.5")
            awaitItem()

            // 2. Sledujeme eventy pro navigaci
            viewModel.eventFlow.test {
                // 3. Uložit
                viewModel.saveMeasurement()

                // 4. Očekáváme navigaci zpět
                val event = awaitItem()
                assertEquals(AddEditMeasurementEvent.NavigateBack, event)

                // 5. Ověříme uložení
                coVerify(exactly = 1) {
                    mockMeasurementRepository.insertMeasurementWithValues(
                        match { it.categoryId == 1L },
                        match { it.size == 1 && it[0].value == 85.5 }
                    )
                }
            }
        }
    }
}