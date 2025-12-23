package cz.tomasjanicek.bp.ui.screens.examination.addEdit

import app.cash.turbine.test
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.model.Doctor
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

class AddEditExaminationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockExaminationRepository = mockk<ILocalExaminationsRepository>(relaxed = true)
    private val mockDoctorRepository = mockk<ILocalDoctorsRepository>()

    private lateinit var viewModel: AddEditExaminationViewModel

    private val dummyDoctor = Doctor(id = 1, name = "MUDr. House", specialization = "Diagnostika")

    @Before
    fun setup() {
        coEvery { mockDoctorRepository.getAll() } returns flowOf(listOf(dummyDoctor))
        viewModel = AddEditExaminationViewModel(mockExaminationRepository, mockDoctorRepository)
    }

    @Test
    fun `loadExamination with null ID initializes new examination`() = runTest {
        viewModel.addEditExaminationUIState.test {
            // 1. Čekáme na Loading (initial state)
            val initialState = awaitItem()
            assertTrue(initialState is AddEditExaminationUIState.Loading)

            // 2. Akce: Načíst (uvnitř test bloku)
            viewModel.loadExamination(null)

            // 3. Čekáme na Loaded
            val loadedState = awaitItem() as AddEditExaminationUIState.ExaminationChanged

            // OPRAVA: ID nového záznamu je null
            assertNull("New examination ID should be null", loadedState.data.examination.id)
            assertEquals(1, loadedState.data.doctors.size)
        }
    }

    @Test
    fun `saveExamination with empty purpose shows error`() = runTest {
        viewModel.addEditExaminationUIState.test {
            // 1. Initial Loading
            awaitItem()

            // 2. Načíst data
            viewModel.loadExamination(null)
            val loadedState = awaitItem() // ExaminationChanged
            assertTrue(loadedState is AddEditExaminationUIState.ExaminationChanged)

            // 3. Akce: Uložit s chybou
            viewModel.saveExamination()

            // 4. Čekáme na stav s chybou
            val errorState = awaitItem() as AddEditExaminationUIState.ExaminationChanged

            assertEquals(R.string.error_field_required, errorState.data.purposeError)
            coVerify(exactly = 0) { mockExaminationRepository.insert(any()) }
        }
    }

    @Test
    fun `saveExamination with valid data calls repository`() = runTest {
        viewModel.addEditExaminationUIState.test {
            // 1. Initial Loading
            awaitItem()

            // 2. Načíst data
            viewModel.loadExamination(null)
            awaitItem() // ExaminationChanged (ignorujeme detaily, zajímá nás až save)

            // 3. Vyplnit data
            viewModel.onPurposeChanged("Preventivní prohlídka")
            awaitItem() // State update po změně textu

            viewModel.onDoctorChanged(1L)
            awaitItem() // State update po změně doktora

            // 4. Uložit
            viewModel.saveExamination()

            // 5. Čekáme na Saved
            val savedState = awaitItem()
            assertTrue(savedState is AddEditExaminationUIState.ExaminationSaved)

            // Ověření volání repozitáře
            coVerify(exactly = 1) {
                mockExaminationRepository.insert(match {
                    it.purpose == "Preventivní prohlídka" && it.doctorId == 1L
                })
            }
        }
    }
}