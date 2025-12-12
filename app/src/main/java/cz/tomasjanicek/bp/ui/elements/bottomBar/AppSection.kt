package cz.tomasjanicek.bp.ui.elements.bottomBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppSection(
    val id: Int, // Původní index (pro zpětnou kompatibilitu)
    val title: String,
    val icon: ImageVector,
    val defaultEnabled: Boolean = true,
    val canBeDisabled: Boolean = true // První obrazovku (Home) raději nevypínat
) {
    EXAMINATIONS(0, "Prohlídky", Icons.Filled.List, true, false), // Nelze vypnout
    MEASUREMENTS(1, "Měření", Icons.Filled.MonitorHeart, true, true),
    MEDICINE(2, "Léky", Icons.Filled.Medication, true, true),
    STATS(3, "Statistiky", Icons.Filled.BarChart, true, true),
    CYCLE(4, "Cyklus", Icons.Filled.WaterDrop, true, true);

    companion object {
        // Pomocná metoda pro získání sekce podle starého indexu
        fun getByIndex(index: Int): AppSection = entries.find { it.id == index } ?: EXAMINATIONS
    }
}