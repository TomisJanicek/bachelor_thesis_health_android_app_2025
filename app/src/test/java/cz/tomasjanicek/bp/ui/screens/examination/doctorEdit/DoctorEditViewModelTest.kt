package cz.tomasjanicek.bp.ui.screens.examination.doctorEdit

import android.content.Context
import app.cash.turbine.test
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.rules.MainDispatcherRule
import cz.tomasjanicek.bp.utils.EmailValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DoctorEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockContext = mockk<Context>(relaxed = true)
    private val mockDoctorRepository = mockk<ILocalDoctorsRepository>(relaxed = true)

    private lateinit var viewModel: DoctorEditViewModel

    private val dummyDoctor = Doctor(id = 1, name = "Dr. Janíček", specialization = "Diagnostika", email = "janicek@hospital.com")

    @Before
    fun setup() {
        // --- MOCKOVÁNÍ NAŠEHO VALIDÁTORU ---
        // Říkáme MockK, aby sledoval náš objekt EmailValidator
        mockkObject(EmailValidator)

        // Výchozí chování: e-mail je vždy validní
        every { EmailValidator.isValid(any()) } returns true

        // --- PŘÍPRAVA REPOZITÁŘE ---
        coEvery { mockDoctorRepository.getDoctorWithData(1L) } returns flowOf(dummyDoctor)

        // Inicializace ViewModelu
        viewModel = DoctorEditViewModel(mockContext, mockDoctorRepository)
    }

    @After
    fun tearDown() {
        // Důležité: Uvolnit mock objektu, aby neovlivňoval jiné testy
        unmockkObject(EmailValidator)
    }

    @Test
    fun `subscribeToDoctorUpdates loads doctor data successfully`() = runTest {
        viewModel.subscribeToDoctorUpdates(1L)

        viewModel.uiState.test {
            // Přeskočíme případný loading
            var currentState = awaitItem()
            while (currentState !is DoctorEditUIState.Success) {
                currentState = awaitItem()
            }

            assertEquals("Dr. Janíček", currentState.data.doctor?.name)
        }
    }

    @Test
    fun `saveDoctor with empty name shows error`() = runTest {
        // ARRANGE: Doktor bez jména
        val invalidDoctor = dummyDoctor.copy(name = "")
        coEvery { mockDoctorRepository.getDoctorWithData(1L) } returns flowOf(invalidDoctor)

        viewModel.subscribeToDoctorUpdates(1L)

        viewModel.uiState.test {
            var currentState = awaitItem()
            while (currentState !is DoctorEditUIState.Success) { currentState = awaitItem() }

            // ACT
            viewModel.saveDoctor()

            // ASSERT
            val errorState = awaitItem() as DoctorEditUIState.Success
            assertEquals(R.string.error_field_required, errorState.data.nameError)

            coVerify(exactly = 0) { mockDoctorRepository.update(any()) }
        }
    }

    @Test
    fun `saveDoctor with valid data calls repository`() = runTest {
        viewModel.subscribeToDoctorUpdates(1L)

        viewModel.uiState.test {
            var currentState = awaitItem()
            while (currentState !is DoctorEditUIState.Success) { currentState = awaitItem() }

            viewModel.onNameChanged("Dr. Gregory House")
            awaitItem()

            viewModel.saveDoctor()

            val savedState = awaitItem()
            assertTrue(savedState is DoctorEditUIState.DoctorSaved)

            coVerify(exactly = 1) {
                mockDoctorRepository.update(match { it.name == "Dr. Gregory House" })
            }
        }
    }

    @Test
    fun `onEmailChanged validates email format`() = runTest {
        // ARRANGE: Nastavíme mock validátoru, aby vracel FALSE pro konkrétní vstup
        every { EmailValidator.isValid("bad-email") } returns false

        viewModel.subscribeToDoctorUpdates(1L)

        viewModel.uiState.test {
            var currentState = awaitItem()
            while (currentState !is DoctorEditUIState.Success) { currentState = awaitItem() }

            // ACT
            viewModel.onEmailChanged("bad-email")

            // ASSERT
            val errorState = awaitItem() as DoctorEditUIState.Success
            assertEquals(R.string.error_invalid_email, errorState.data.emailError)
        }
    }
}