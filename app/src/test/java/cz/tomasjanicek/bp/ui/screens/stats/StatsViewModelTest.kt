package cz.tomasjanicek.bp.ui.screens.stats

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementsRepository
import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import cz.tomasjanicek.bp.model.MeasurementWithValues
import cz.tomasjanicek.bp.rules.MainDispatcherRule
import cz.tomasjanicek.bp.services.PdfExporter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockCategoriesRepo = mockk<ILocalMeasurementCategoriesRepository>(relaxed = true)
    private val mockMeasurementsRepo = mockk<ILocalMeasurementsRepository>(relaxed = true)
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockUri = mockk<Uri>()

    private lateinit var viewModel: StatsViewModel

    // Data
    private val category = MeasurementCategory(id = 1, name = "Tlak")
    private val categoryWithFields = MeasurementCategoryWithFields(
        category = category,
        fields = emptyList()
    )
    private val measurement = Measurement(id = 10, categoryId = 1, measuredAt = System.currentTimeMillis())
    private val measurementWithValues = MeasurementWithValues(
        measurement = measurement,
        values = emptyList(),
        category = category
    )

    @Before
    fun setup() {
        // Mockujeme konstruktor PdfExporteru
        mockkConstructor(PdfExporter::class)

        // Mockujeme metodu exportStatsToPdf
        every {
            anyConstructed<PdfExporter>().exportStatsToPdf(
                chartData = any<List<StatsChartData>>(),
                periodType = any<StatsPeriodType>(),
                startDate = any<LocalDate>(),
                endDate = any<LocalDate>()
            )
        } returns mockUri

        // Nastavíme repozitáře
        coEvery { mockCategoriesRepo.getAllCategoriesWithFields() } returns flowOf(listOf(categoryWithFields))
        coEvery {
            mockMeasurementsRepo.getMeasurementsWithValuesBetween(any(), any())
        } returns flowOf(listOf(measurementWithValues))

        viewModel = StatsViewModel(mockCategoriesRepo, mockMeasurementsRepo, mockContext)
    }

    @After
    fun tearDown() {
        unmockkConstructor(PdfExporter::class)
    }

    @Test
    fun `initial state loads data correctly`() = runTest {
        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            assertEquals(false, state.isLoading)
            assertEquals(1, state.allCategories.size)
            assertEquals("Tlak", state.allCategories[0].name)
        }
    }

    @Test
    fun `selecting category updates chart data`() = runTest {
        viewModel.uiState.test {
            // Skip loading
            var state = awaitItem()
            while (state.isLoading) { state = awaitItem() }

            assertTrue(state.selectedCategoryIds.isEmpty())

            // ACT: Vybrat kategorii
            viewModel.onAction(StatsAction.OnCategorySelectionChanged(1L, true))

            // ASSERT: Nový stav
            state = awaitItem()
            assertTrue(state.selectedCategoryIds.contains(1L))
            assertEquals(1, state.chartData.size)
        }
    }

    @Test
    fun `export to PDF calls exporter and updates URI`() = runTest {
        // 1. Připravíme data (vybereme kategorii)
        viewModel.onAction(StatsAction.OnCategorySelectionChanged(1L, true))

        // Počkáme, až se data načtou (synchronizace flow)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.chartData.isEmpty()) { state = awaitItem() }
        }

        // 2. Sledujeme Flow s URI a TEPRVE PAK voláme akci
        viewModel.exportedFileUri.test {
            // A) Počáteční stav musí být null
            assertNull("Initial URI should be null", awaitItem())

            // B) ACT: Klikneme na export (uvnitř test bloku!)
            viewModel.onAction(StatsAction.OnExportClicked)

            // C) ASSERT: Musí přijít mockUri
            val resultUri = awaitItem()
            assertNotNull("URI should not be null after export", resultUri)
            assertEquals(mockUri, resultUri)
        }
    }

    @Test
    fun `onExportHandled resets URI to null`() = runTest {
        // 1. Příprava dat
        viewModel.onAction(StatsAction.OnCategorySelectionChanged(1L, true))
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.chartData.isEmpty()) { state = awaitItem() }
        }

        viewModel.exportedFileUri.test {
            assertNull(awaitItem()) // Initial

            // 2. Provedeme export
            viewModel.onAction(StatsAction.OnExportClicked)
            assertEquals(mockUri, awaitItem()) // Dostaneme URI

            // 3. ACT: Resetujeme (uživatel sdílel soubor)
            viewModel.onExportHandled()

            // 4. ASSERT: Musí se vrátit na null
            assertNull("URI should be reset to null", awaitItem())
        }
    }
}