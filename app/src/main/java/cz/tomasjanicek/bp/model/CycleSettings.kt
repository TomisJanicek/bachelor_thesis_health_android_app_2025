package cz.tomasjanicek.bp.model

data class CycleSettings(
    val averageCycleLength: Int = 28,     // Průměrná délka cyklu v dnech
    val averageMenstruationLength: Int =5 // Průměrná délka menstruace v dnech
)