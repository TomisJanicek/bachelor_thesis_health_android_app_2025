package cz.tomasjanicek.bp.ui.screens.stats

import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import cz.tomasjanicek.bp.model.MeasurementWithValues
import java.time.LocalDate

/**
 * Předdefinované typy časových období pro statistiky.
 */
enum class StatsPeriodType(val label: String) {
    DAY("Den"),
    WEEK("Týden"),
    MONTH("Měsíc"),
    YEAR("Rok"),
    CUSTOM("Od-Do")
}

/**
 * Data potřebná pro vykreslení jednoho grafu ve statistikách.
 */
data class StatsChartData(
    val categoryWithFields: MeasurementCategoryWithFields,
    val measurementsWithValues: List<MeasurementWithValues>
)

/**
 * Reprezentuje stav UI obrazovky se statistikami.
 */
data class StatsState(
    /** Zda se načítají počáteční data (seznam kategorií). */
    val isLoading: Boolean = true,

    // --- Filtry ---
    /** Seznam všech dostupných kategorií měření v aplikaci. */
    val allCategories: List<MeasurementCategory> = emptyList(),
    /** Množina ID kategorií, které si uživatel vybral pro zobrazení. */
    val selectedCategoryIds: Set<Long> = emptySet(),
    /** Vybraný typ období (Den, Týden, ...). */
    val selectedPeriodType: StatsPeriodType = StatsPeriodType.WEEK,
    /** Počáteční datum pro vlastní rozsah (použito pro 'CUSTOM'). */
    val customStartDate: LocalDate = LocalDate.now().minusWeeks(1),
    /** Koncové datum pro vlastní rozsah (použito pro 'CUSTOM'). */
    val customEndDate: LocalDate = LocalDate.now(),

    // --- Výsledná data ---
    /** Seznam dat pro jednotlivé grafy na základě filtrů. */
    val chartData: List<StatsChartData> = emptyList()
)