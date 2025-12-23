package cz.tomasjanicek.bp.ui.screens.cycle.components

import cz.tomasjanicek.bp.model.CycleRecord
import cz.tomasjanicek.bp.model.CycleSettings
import cz.tomasjanicek.bp.model.EventType
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class CycleCalculatorTest {

    private val calculator = CycleCalculator()

    @Test
    fun `groupRecordsIntoCycles groups consecutive days correctly`() {
        // ARRANGE (Příprava)
        // Simulujeme 3 dny menstruace za sebou (1.1., 2.1., 3.1.)
        // a pak další menstruaci o měsíc později (1.2., 2.2.)
        val records = listOf(
            createRecord("2024-01-01"),
            createRecord("2024-01-02"),
            createRecord("2024-01-03"),
            createRecord("2024-02-01"),
            createRecord("2024-02-02")
        )

        // ACT (Akce)
        val result = calculator.groupRecordsIntoCycles(records)

        // ASSERT (Ověření)
        // Měli bychom mít 2 cykly
        Assert.assertEquals(2, result.size)

        // První cyklus v seznamu je ten NEJNOVĚJŠÍ (únor)
        Assert.assertEquals(LocalDate.parse("2024-02-01"), result[0].startDate)
        Assert.assertEquals(2, result[0].menstruationDays.size)

        // Druhý cyklus je ten starší (leden)
        Assert.assertEquals(LocalDate.parse("2024-01-01"), result[1].startDate)
        Assert.assertEquals(3, result[1].menstruationDays.size)
    }

    @Test
    fun `calculateAverages returns correct averages for regular cycles`() {
        // ARRANGE
        // Máme 3 cykly s délkou 28 dní mezi začátky
        // 1. cyklus: 1.1. (délka menstruace 5 dní)
        // 2. cyklus: 29.1. (délka menstruace 5 dní)
        // 3. cyklus: 26.2. (délka menstruace 5 dní)
        val cycles = listOf(
            createCycle("2024-02-26", 5), // Nejnovější
            createCycle("2024-01-29", 5),
            createCycle("2024-01-01", 5)  // Nejstarší
        )

        // ACT
        val (avgCycle, avgMens) = calculator.calculateAverages(cycles)

        // ASSERT
        // (28 + 28) / 2 = 28
        Assert.assertEquals(28, avgCycle)
        Assert.assertEquals(5, avgMens)
    }

    @Test
    fun `calculateAverages returns zero when less than 2 cycles`() {
        // ARRANGE - máme jen jeden cyklus, z toho se průměr nepočítá
        val cycles = listOf(createCycle("2024-01-01", 5))

        // ACT
        val (avgCycle, _) = calculator.calculateAverages(cycles)

        // ASSERT
        Assert.assertEquals(0, avgCycle)
    }

    @Test
    fun `predictFuture uses settings when no history available`() {
        // ARRANGE
        val settings = CycleSettings(averageCycleLength = 30, averageMenstruationLength = 4)
        val today = LocalDate.now()

        // ACT
        val predictions = calculator.predictFuture(
            historicalCycles = emptyList(), // Žádná historie
            allRecords = emptyList(),
            settings = settings
        )

        // ASSERT
        // Očekáváme 4 predikce (aktuální + 3 budoucí)
        Assert.assertEquals(4, predictions.size)

        val firstPrediction = predictions[0]

        // Délka predikované menstruace by měla být 4 dny (podle settings)
        Assert.assertEquals(4, firstPrediction.fullMenstruationEstimate.size)

        // Ovulace je 14 dní před koncem cyklu (30 - 14 = 16. den)
        // Logika: nextCycleStart = today + 30; ovulation = nextStart - 14
        val expectedOvulation = today.plusDays(16)

        Assert.assertEquals(expectedOvulation, firstPrediction.ovulationDay)
    }

    // --- Pomocné metody pro testy ---

    private fun createRecord(dateString: String): CycleRecord {
        return CycleRecord(LocalDate.parse(dateString), EventType.MENSTRUATION)
    }

    private fun createCycle(startDateString: String, daysCount: Int): Cycle {
        val start = LocalDate.parse(startDateString)
        val days = (0 until daysCount).map { start.plusDays(it.toLong()) }
        return Cycle(start, days)
    }
}