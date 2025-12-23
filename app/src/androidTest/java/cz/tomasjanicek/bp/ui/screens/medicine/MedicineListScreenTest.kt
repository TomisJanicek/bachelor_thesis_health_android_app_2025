package cz.tomasjanicek.bp.ui.screens.medicine

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cz.tomasjanicek.bp.TestActivity
import cz.tomasjanicek.bp.di.FakeDatabase
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.model.MedicineUnit
import cz.tomasjanicek.bp.model.RegularityType
import cz.tomasjanicek.bp.model.ReminderStatus
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.screens.medicine.list.MedicineListScreen
import cz.tomasjanicek.bp.ui.theme.BpTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@HiltAndroidTest
class MedicineListScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    private val mockRouter = mockk<INavigationRouter>(relaxed = true)

    @Before
    fun setup() {
        hiltRule.inject()
        // Vyčistíme fake databázi, aby se testy neovlivňovaly
        FakeDatabase.clear()

        // --- PŘÍPRAVA DAT ---
        runBlocking {
            // 1. Vytvoření léku (Ibalgin)
            val medicine = Medicine(
                id = 1L,
                name = "Ibalgin",
                dosage = 400.0,
                unit = MedicineUnit.MG,
                isRegular = true,
                regularityType = RegularityType.DAILY,
                regularTimes = listOf(480), // 8:00
                note = "Na bolest hlavy"
            )
            FakeDatabase.medicines.add(medicine)

            // 2. Vytvoření připomínky na DNEŠEK v 8:00
            // Musíme vypočítat timestamp (Long)
            val today8am = java.time.LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0))
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val reminder = MedicineReminder(
                id = 100L,
                medicineId = 1L,
                plannedDateTime = today8am, // <-- Timestamp
                status = ReminderStatus.PLANNED // <-- Enum
            )
            FakeDatabase.reminders.add(reminder)
        }

        // --- SPUŠTĚNÍ UI ---
        composeTestRule.setContent {
            BpTheme {
                MedicineListScreen(
                    navigationRouter = mockRouter,
                    currentScreenIndex = 1 // Index pro léky
                )
            }
        }
    }

    @Test
    fun displaysPlannedMedicine() {
        composeTestRule.waitForIdle()

        // 1. Ověříme hlavičku "Naplánované"
        composeTestRule.onNodeWithText("Naplánované").assertIsDisplayed()

        // 2. Ověříme název léku
        composeTestRule.onNodeWithText("Ibalgin").assertIsDisplayed()

        // 3. Ověříme dávku (UI formátuje 400.0 na "400" a přidá jednotku)
        // Hledáme text "400 mg" (nebo část textu)
        composeTestRule.onNodeWithText("400 mg", substring = true).assertIsDisplayed()
    }

    @Test
    fun clickingItemOpensDetailDialog() {
        composeTestRule.waitForIdle()

        // 1. Klikneme na název léku (to by mělo otevřít dialog podle vaší implementace onCheckedChange vs onClick)
        composeTestRule.onNodeWithText("Ibalgin").performClick()

        composeTestRule.waitForIdle()

        // 2. Ověříme, že se zobrazil Dialog
        // Hledáme text z dialogu (název léku v titulku nebo poznámku)
        composeTestRule.onNodeWithText("Poznámka: Na bolest hlavy").assertIsDisplayed()

        // 3. Zavřeme dialog
        composeTestRule.onNodeWithText("Zavřít").performClick()

        // 4. Ověříme, že dialog zmizel (text poznámky už tam není)
        composeTestRule.onNodeWithText("Poznámka: Na bolest hlavy").assertDoesNotExist()
    }

    @Test
    fun switchingToTomorrowShowsEmptyState() {
        composeTestRule.waitForIdle()

        // 1. Klikneme na šipku doprava (další den)
        composeTestRule.onNodeWithContentDescription("Následující den").performClick()

        composeTestRule.waitForIdle()

        // 2. Ověříme, že Ibalgin zmizel (protože jsme ho vložili jen na dnešek)
        composeTestRule.onNodeWithText("Ibalgin").assertDoesNotExist()

        // 3. Ověříme prázdný stav
        // Text: "Pro tento den nemáte žádné léky."
        composeTestRule.onNodeWithText("nemáte žádné léky", substring = true).assertIsDisplayed()
    }

    @Test
    fun dateSelectorShowsToday() {
        composeTestRule.waitForIdle()
        // Ověříme, že v horní liště je napsáno "Dnes"
        composeTestRule.onNodeWithText("Dnes", substring = true).assertIsDisplayed()
    }
}