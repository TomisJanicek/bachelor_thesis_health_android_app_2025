package cz.tomasjanicek.bp.ui.screens.injection

import app.cash.turbine.test
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.database.injection.IInjectionRepository
import cz.tomasjanicek.bp.model.Injection
import cz.tomasjanicek.bp.model.InjectionCategory
import cz.tomasjanicek.bp.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditInjectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository = mockk<IInjectionRepository>(relaxed = true)
    private lateinit var viewModel: AddEditInjectionViewModel

    private val dummyInjection = Injection(
        id = 10L,
        name = "Tetanus",
        disease = "Tetanus",
        category = InjectionCategory.RECOMMENDED,
        date = 123456789L
    )

    @Before
    fun setup() {
        // Nastavíme mock repozitáře, aby vracel naše data
        coEvery { mockRepository.getInjectionById(10L) } returns flowOf(dummyInjection)

        viewModel = AddEditInjectionViewModel(mockRepository)
    }

    @Test
    fun `loadInjection with null ID initializes empty data`() = runTest {
        viewModel.uiState.test {
            // 1. Initial Loading
            val initialState = awaitItem()
            assertTrue(initialState is AddEditInjectionUIState.Loading)

            // 2. Načíst "nový" (null ID)
            viewModel.loadInjection(null)

            // 3. Očekáváme Success s prázdnými daty
            val loadedState = awaitItem() as AddEditInjectionUIState.Success

            assertEquals(0L, loadedState.data.injection.id)
            assertEquals("", loadedState.data.injection.name)
        }
    }

    @Test
    fun `loadInjection with valid ID loads data from repository`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Loading

            // 2. Načíst existující ID (10L)
            viewModel.loadInjection(10L)

            // 3. Očekáváme Success s daty z dummyInjection
            val loadedState = awaitItem() as AddEditInjectionUIState.Success

            assertEquals(10L, loadedState.data.injection.id)
            assertEquals("Tetanus", loadedState.data.injection.name)
        }
    }

    @Test
    fun `saveInjection with empty fields shows errors`() = runTest {
        // 1. Načteme prázdný formulář
        viewModel.loadInjection(null)

        viewModel.uiState.test {
            // Odfiltrujeme stavy Loading a první Success (načtení)
            var currentState = awaitItem()
            while (currentState !is AddEditInjectionUIState.Success) {
                currentState = awaitItem()
            }

            // 2. ACT: Zkusíme uložit prázdná data
            viewModel.saveInjection()

            // 3. ASSERT:
            // saveInjection volá onNameChanged -> onDiseaseChanged.
            // To vyvolá 2 změny stavu. Musíme přečíst obě (nebo tu první ignorovat).

            val firstUpdate = awaitItem() as AddEditInjectionUIState.Success // Stav po onNameChanged
            val secondUpdate = awaitItem() as AddEditInjectionUIState.Success // Stav po onDiseaseChanged (ten nás zajímá)

            // Kontrolujeme až ten finální stav
            assertEquals(R.string.error_field_required, secondUpdate.data.nameError)
            assertEquals(R.string.error_field_required, secondUpdate.data.diseaseError)

            // Repozitář se nesměl zavolat
            coVerify(exactly = 0) { mockRepository.saveInjection(any()) }
        }
    }

    @Test
    fun `saveInjection with valid data calls repository`() = runTest {
        // 1. Načteme
        viewModel.loadInjection(null)

        viewModel.uiState.test {
            var currentState = awaitItem()
            while (currentState !is AddEditInjectionUIState.Success) { currentState = awaitItem() }

            // 2. ACT: Vyplníme validní data
            viewModel.onNameChanged("Chřipka")
            awaitItem() // State update po změně jména

            viewModel.onDiseaseChanged("Chřipka")
            awaitItem() // State update po změně nemoci

            // 3. Uložit
            viewModel.saveInjection()

            // 4. ASSERT: Očekáváme stav Saved
            val savedState = awaitItem()
            assertTrue(savedState is AddEditInjectionUIState.InjectionSaved)

            // Ověříme volání repozitáře
            coVerify(exactly = 1) {
                mockRepository.saveInjection(match {
                    it.name == "Chřipka" && it.disease == "Chřipka"
                })
            }
        }
    }

    @Test
    fun `deleteInjection calls repository`() = runTest {
        // 1. Načteme existující záznam (aby měl ID != 0)
        viewModel.loadInjection(10L)

        viewModel.uiState.test {
            var currentState = awaitItem()
            while (currentState !is AddEditInjectionUIState.Success) { currentState = awaitItem() }

            // 2. ACT: Smazat
            viewModel.deleteInjection()

            // 3. ASSERT: Stav Deleted
            val deletedState = awaitItem()
            assertTrue(deletedState is AddEditInjectionUIState.InjectionDeleted)

            // Ověříme volání repozitáře s ID 10
            coVerify(exactly = 1) { mockRepository.deleteInjection(10L) }
        }
    }
}