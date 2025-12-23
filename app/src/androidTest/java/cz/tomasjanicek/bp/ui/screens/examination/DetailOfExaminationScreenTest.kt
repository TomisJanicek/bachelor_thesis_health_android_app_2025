package cz.tomasjanicek.bp.ui.screens.examination

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.printToLog
import cz.tomasjanicek.bp.TestActivity
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.di.FakeDatabase
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationStatus
import cz.tomasjanicek.bp.model.ExaminationType
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.screens.examination.detail.DetailOfExaminationScreen
import cz.tomasjanicek.bp.ui.theme.BpTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import javax.inject.Inject

@HiltAndroidTest
class DetailOfExaminationScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Inject
    lateinit var doctorsRepository: ILocalDoctorsRepository

    @Inject
    lateinit var examinationsRepository: ILocalExaminationsRepository

    private val mockRouter = mockk<INavigationRouter>(relaxed = true)

    private val TEST_DOCTOR_ID = 99L

    @Before
    fun setup() {
        hiltRule.inject()

        // DŮLEŽITÉ: Vyčistit sdílenou paměť před každým testem
        FakeDatabase.clear()

        runBlocking {
            // 1. Vložíme testovacího lékaře
            val doctor = Doctor(
                id = TEST_DOCTOR_ID,
                name = "MUDr. Testovací Lékař",
                specialization = "Kardiologie",
                phone = "123456789",
                email = "doktor@test.cz",
                addressLabel = "Nemocnice na kraji města"
            )
            doctorsRepository.insert(doctor)

            // 2. Vložíme BUDOUCÍ prohlídku (zítra)
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            examinationsRepository.insert(
                Examination(
                    id = 101,
                    doctorId = TEST_DOCTOR_ID,
                    purpose = "Kontrola srdce (Budoucí)",
                    dateTime = tomorrow,
                    status = ExaminationStatus.PLANNED,
                    type = ExaminationType.PROHLIDKA,
                    note = "Poznámka",
                    result = null
                )
            )

            // 3. Vložíme MINULOU prohlídku (včera) - TOTO VÁM CHYBĚLO
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
            examinationsRepository.insert(
                Examination(
                    id = 102,
                    doctorId = TEST_DOCTOR_ID,
                    purpose = "Vstupní prohlídka (Minulá)",
                    dateTime = yesterday,
                    status = ExaminationStatus.COMPLETED,
                    type = ExaminationType.PROHLIDKA, // nebo VYSETRENI
                    note = "Hotovo",
                    result = "OK"
                )
            )
        }

        composeTestRule.setContent {
            BpTheme {
                DetailOfExaminationScreen(
                    navigationRouter = mockRouter,
                    doctorId = TEST_DOCTOR_ID
                )
            }
        }

        // Čekáme, až se data načtou a UI se překreslí
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Testovací", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun displaysDoctorInfoCorrectly() {
        // composeTestRule.onRoot().printToLog("UI_TREE") // Debug jen když je potřeba
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("MUDr. Testovací Lékař", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Kardiologie", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("123456789", substring = true).assertIsDisplayed()
    }

    @Test
    fun showsScheduledExaminationsByDefault() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Naplánované").assertIsSelected()

        composeTestRule.onNodeWithText("Kontrola srdce (Budoucí)")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Vstupní prohlídka (Minulá)").assertDoesNotExist()
    }

    @Test
    fun switchingToHistoryShowsPastExaminations() {
        composeTestRule.waitForIdle()

        // Klikneme na tab "Historie"
        composeTestRule.onNodeWithText("Historie").performClick()

        // Dáme tomu chvilku (waitUntil je bezpečnější než waitForIdle při změnách seznamu)
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Minulá", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Vstupní prohlídka (Minulá)")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Kontrola srdce (Budoucí)").assertDoesNotExist()
    }

    @Test
    fun contactButtonsAreDisplayed() {
        composeTestRule.onNodeWithText("Zavolat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigace").assertIsDisplayed()
    }
}