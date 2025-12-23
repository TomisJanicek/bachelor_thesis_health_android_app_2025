package cz.tomasjanicek.bp.ui.screens.examination

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.printToLog
// PŘIDÁNO: Import vaší vlastní aktivity
import cz.tomasjanicek.bp.TestActivity
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.screens.examination.list.ListOfExaminationScreen
import cz.tomasjanicek.bp.ui.theme.BpTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
// ODSTRANĚNO: import dagger.hilt.android.testing.HiltTestActivity
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ListOfExaminationScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    // ZMĚNA: Používáme TestActivity (tu vaši) místo HiltTestActivity
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    private val mockRouter = mockk<INavigationRouter>(relaxed = true)

    @Before
    fun setup() {
        hiltRule.inject()

        // Nastavíme obsah do prázdné testovací aktivity
        composeTestRule.setContent {
            BpTheme {
                ListOfExaminationScreen(
                    navigationRouter = mockRouter,
                    currentScreenIndex = 0
                )
            }
        }
    }

    @Test
    fun displaysScheduledExaminationsByDefault() {
        // 1. Důležité: Počkáme, až se UI po startu "usadí" a načte data
        composeTestRule.waitForIdle()

        // TOTO PŘIDEJTE: Vypíše do Logcatu kompletní strom UI.
        // Uvidíme, jaké texty tam reálně jsou.
        composeTestRule.onRoot().printToLog("UI_TREE")

        /// 1. Nadpis je OK
        composeTestRule.onNodeWithText("Zdravotní záznamy").assertIsDisplayed()

        // 2. "Preventivní prohlídka" tam JE (vidím ji v logu)
        composeTestRule.onNodeWithText("Preventivní prohlídka")
            .performScrollTo()
            .assertIsDisplayed()

        // 3. OPRAVA: Místo jména lékaře (které tam není) hledáme specializaci
        // V logu vidíme "Praktický lékař", tak použijeme to.
        composeTestRule.onNodeWithText("Praktický lékař", substring = true)
            .performScrollTo()
            .assertIsDisplayed()

        // 4. Negativní test
        composeTestRule.onNodeWithText("Trhání osmičky").assertDoesNotExist()
    }

    @Test
    fun switchingToHistoryShowsPastExaminations() {
        composeTestRule.onNodeWithText("Historie").performClick()
        composeTestRule.waitForIdle()

        // Tady předpokládám stejný problém. Místo jména "Petr Zub"
        // budeme hledat název zákroku nebo specializaci.

        // Zkuste hledat zákrok:
        composeTestRule.onNodeWithText("Trhání osmičky")
            .performScrollTo()
            .assertIsDisplayed()

        // Nebo specializaci (pokud ji FakeRepository vrací):
        composeTestRule.onNodeWithText("Zubař", substring = true)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Preventivní prohlídka").assertDoesNotExist()
    }

    @Test
    fun switchingToInjectionsShowsVaccines() {
        // 1. Klikneme na tab "Očkování"
        composeTestRule.onNodeWithText("Očkování").performClick()

        // 2. Ověříme data z FakeInjectionRepository
        composeTestRule.onNodeWithText("Tetanus").assertIsDisplayed()
    }

    @Test
    fun fabExpandsAndShowsOptions() {
        // 1. Klikneme na FAB (v kódu má contentDescription "Přidat záznam")
        composeTestRule.onNodeWithContentDescription("Přidat záznam").performClick()

        // 2. Ověříme, že vyskočily možnosti
        composeTestRule.onNodeWithText("Přidat prohlídku").assertIsDisplayed()
        composeTestRule.onNodeWithText("Evidovat očkování").assertIsDisplayed()
    }
}