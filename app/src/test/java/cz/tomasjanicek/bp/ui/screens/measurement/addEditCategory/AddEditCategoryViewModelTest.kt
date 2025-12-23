package cz.tomasjanicek.bp.ui.screens.measurement.addEditCategory

import app.cash.turbine.test
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import cz.tomasjanicek.bp.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditCategoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository = mockk<ILocalMeasurementCategoriesRepository>(relaxed = true)
    private lateinit var viewModel: AddEditCategoryViewModel

    private val dummyCategory = MeasurementCategory(id = 5, name = "Krevní tlak")
    private val dummyCategoryWithFields = MeasurementCategoryWithFields(dummyCategory, emptyList())

    @Before
    fun setup() {
        coEvery { mockRepository.getCategoryWithFieldsById(5L) } returns flowOf(dummyCategoryWithFields)
        viewModel = AddEditCategoryViewModel(mockRepository)
    }

    @Test
    fun `loadCategory with null ID initializes empty category`() = runTest {
        viewModel.loadCategory(null)

        viewModel.uiState.test {
            val state = awaitItem() as AddEditCategoryUIState.CategoryChanged
            assertEquals(0L, state.data.category.id)
            assertEquals("", state.data.category.name)
            assertTrue(state.data.fields.isEmpty())
        }
    }

    @Test
    fun `saveCategory with empty name shows error`() = runTest {
        viewModel.loadCategory(null)

        viewModel.uiState.test {
            awaitItem() // Loaded

            // ACT: Uložit prázdné
            viewModel.saveCategory()

            // ASSERT
            val errorState = awaitItem() as AddEditCategoryUIState.CategoryChanged
            assertEquals(R.string.error_field_required, errorState.data.nameError)
            assertEquals(R.string.error_field_type, errorState.data.fieldsError) // Také chyba polí (žádné nejsou)
        }
    }

    @Test
    fun `addParameter flow works correctly`() = runTest {
        viewModel.loadCategory(null)

        viewModel.uiState.test {
            var state = awaitItem() as AddEditCategoryUIState.CategoryChanged

            // 1. Otevřít dialog
            viewModel.onParameterDialogOpened(null)
            state = awaitItem() as AddEditCategoryUIState.CategoryChanged
            assertTrue(state.data.isEditingParameter)
            assertNotNull(state.data.editingField)

            // 2. Vyplnit pole v dialogu
            viewModel.onParameterFieldChanged("Systolický", "mmHg", "0", "200")
            state = awaitItem() as AddEditCategoryUIState.CategoryChanged
            assertEquals("Systolický", state.data.editingField?.label)

            // 3. Uložit dialog
            viewModel.onParameterSaved()
            state = awaitItem() as AddEditCategoryUIState.CategoryChanged

            // 4. Ověřit, že se pole přidalo do seznamu
            assertEquals(1, state.data.fields.size)
            assertEquals("Systolický", state.data.fields[0].label)
            // Dialog by měl být zavřený
            assertEquals(false, state.data.isEditingParameter)
        }
    }

    @Test
    fun `saveCategory with valid data calls repository`() = runTest {
        viewModel.loadCategory(null)

        viewModel.uiState.test {
            awaitItem() // Loaded

            // 1. Jméno
            viewModel.onNameChanged("Tlak")
            awaitItem()

            // 2. Přidat pole (zkráceně - simulace workflow)
            viewModel.onParameterDialogOpened(null); awaitItem()
            viewModel.onParameterFieldChanged("Sys", "", "", ""); awaitItem()
            viewModel.onParameterSaved(); awaitItem()

            // 3. Uložit kategorii
            viewModel.saveCategory()

            // 4. Ověřit
            val savedState = awaitItem()
            assertTrue(savedState is AddEditCategoryUIState.CategorySaved)

            // Ověřit volání repozitáře
            coVerify(exactly = 1) {
                mockRepository.insertCategory(match { it.name == "Tlak" })
            }
            // A také vložení polí
            coVerify(exactly = 1) { mockRepository.insertFields(any()) }
        }
    }
}