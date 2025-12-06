package cz.tomasjanicek.bp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

// Tento enum přidáme pro rozlišení, co se na dni stalo
enum class EventType {
    MENSTRUATION,
    OVULATION
}

// Třída se jmenuje stejně, ale její struktura je úplně jiná
@Entity(tableName = "cycle_records")
data class CycleRecord(
    @PrimaryKey
    val date: LocalDate,
    val eventType: EventType
)