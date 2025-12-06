package cz.tomasjanicek.bp.ui.screens.cycle.components

import cz.tomasjanicek.bp.model.CycleRecord
import cz.tomasjanicek.bp.model.CycleSettings
import cz.tomasjanicek.bp.model.EventType
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

// Pomocná třída pro blok menstruace
data class Cycle(
    val startDate: LocalDate,
    val menstruationDays: List<LocalDate>
)

// Pomocná třída pro vypočítané odhady
data class CyclePredictions(
    val cycleStartDate: LocalDate,
    val fullMenstruationEstimate: List<LocalDate>,
    val fertileWindow: List<LocalDate>,
    val ovulationDay: LocalDate
)

class CycleCalculator @Inject constructor() {

    fun groupRecordsIntoCycles(records: List<CycleRecord>): List<Cycle> {
        val menstruationDays = records
            .filter { it.eventType == EventType.MENSTRUATION }
            .map { it.date }
            .sorted()

        if (menstruationDays.isEmpty()) return emptyList()

        val groups = mutableListOf<MutableList<LocalDate>>()
        groups.add(mutableListOf(menstruationDays.first()))

        for (i in 1 until menstruationDays.size) {
            if (ChronoUnit.DAYS.between(menstruationDays[i - 1], menstruationDays[i]) == 1L) {
                groups.last().add(menstruationDays[i])
            } else {
                groups.add(mutableListOf(menstruationDays[i]))
            }
        }

        // Nejnovější cyklus jako první
        return groups.map { Cycle(startDate = it.first(), menstruationDays = it) }.reversed()
    }

    /**
     * Vrátí průměrnou délku cyklu a menstruace.
     * Pokud je málo dat nebo nesmysly, vrátí 0 → použijí se defaulty ze settings.
     */
    fun calculateAverages(cycles: List<Cycle>): Pair<Long, Long> {
        if (cycles.size < 2) {
            return 0L to 0L
        }

        val cycleLengths = cycles.zipWithNext { current, next ->
            ChronoUnit.DAYS.between(next.startDate, current.startDate)
        }

        val avgCycle = cycleLengths.average().toLong()
        val avgMenstruation = cycles.map { it.menstruationDays.size.toLong() }.average().toLong()

        // Rozumné meze: když je to mimo, radši použít default
        val safeAvgCycle = if (avgCycle in 15..60) avgCycle else 0L
        val safeAvgMens = if (avgMenstruation in 2..10) avgMenstruation else 0L

        return safeAvgCycle to safeAvgMens
    }

    /**
     * Predikce budoucích cyklů:
     * - když nejsou data → jede podle settings (default 28 / 5)
     * - když má data → použije průměry, ale pořád generuje budoucnost
     * - vždy vygeneruje: aktuální cyklus + 3 další
     */
    fun predictFuture(
        historicalCycles: List<Cycle>,
        allRecords: List<CycleRecord>,
        settings: CycleSettings
    ): List<CyclePredictions> {

        val (avgCycleReal, avgMenstruationReal) = calculateAverages(historicalCycles)
        val useDefault = avgCycleReal == 0L || avgMenstruationReal == 0L

        val cycleLength = if (useDefault) settings.averageCycleLength.toLong() else avgCycleReal
        val menstruationLength = if (useDefault) settings.averageMenstruationLength.toLong() else avgMenstruationReal

        // Bez rozumné délky cyklu nemá smysl predikovat
        if (cycleLength !in 15..60) return emptyList()

        val predictions = mutableListOf<CyclePredictions>()

        // Základní začátek:
        // - pokud máme cykly → poslední začátek menstruace
        // - jinak → dnešek jako "den 1"
        var currentCycleStartDate = historicalCycles.firstOrNull()?.startDate ?: LocalDate.now()

        repeat(4) { // aktuální + 3 budoucí
            val nextCycleStartDate = currentCycleStartDate.plusDays(cycleLength)

            // Výpočet ovulace: 14 dní PŘED KONCEM cyklu
            var estimatedOvulation = nextCycleStartDate.minusDays(14)

            // Pokud je v daném cyklu ručně zaznamenaná ovulace, použijeme ji
            allRecords.find { record ->
                record.eventType == EventType.OVULATION &&
                        !record.date.isBefore(currentCycleStartDate) &&
                        record.date.isBefore(nextCycleStartDate)
            }?.let { confirmed ->
                estimatedOvulation = confirmed.date
            }

            // Plodné dny: 5 dní před, den ovulace, 1 den po
            val fertileDays = (-5L..1L).map { offset ->
                estimatedOvulation.plusDays(offset)
            }

            // Menstruace: od začátku cyklu, v délce menstruationLength
            val fullMenstruationEstimate = (0 until menstruationLength).map { offset ->
                currentCycleStartDate.plusDays(offset.toLong())
            }

            predictions.add(
                CyclePredictions(
                    cycleStartDate = currentCycleStartDate,
                    fullMenstruationEstimate = fullMenstruationEstimate,
                    fertileWindow = fertileDays,
                    ovulationDay = estimatedOvulation
                )
            )

            // Posun na další cyklus
            currentCycleStartDate = nextCycleStartDate
        }

        return predictions
    }
}