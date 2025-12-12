package cz.tomasjanicek.bp.ui.elements.bottomBar

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomBar(
    navigationRouter: INavigationRouter,
    currentScreenIndex: Int, // Toto je "absolutní" index (0=Prohlídky, 4=Cyklus)
    viewModel: BottomBarViewModel = hiltViewModel()
) {
    // Sledujeme, které sekce jsou povolené
    val enabledSections by viewModel.enabledSections.collectAsState()

    // Vyfiltrujeme seznam všech sekcí a necháme jen ty povolené a seřadíme je podle ID
    val visibleItems = remember(enabledSections) {
        AppSection.values()
            .filter { it in enabledSections }
            .sortedBy { it.id }
    }

    // Zjistíme, která sekce je aktuálně aktivní podle "absolutního" indexu
    val currentSection = AppSection.getByIndex(currentScreenIndex)

    NavigationBar(
        modifier = Modifier.padding(0.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp
    ) {
        visibleItems.forEach { item ->
            NavigationBarItem(
                // Porovnáváme objekty (Enumy), ne indexy v poli!
                selected = currentSection == item,
                onClick = {
                    // Navigace podle Enumu
                    when (item) {
                        AppSection.EXAMINATIONS -> navigationRouter.navigaTetoListOfExaminationView()
                        AppSection.MEASUREMENTS -> navigationRouter.navigateToListOfMeasurementCategories()
                        AppSection.MEDICINE -> navigationRouter.navigateToMedicineList()
                        AppSection.STATS -> navigationRouter.navigateToStatsScreen()
                        AppSection.CYCLE -> navigationRouter.navigateToCycleScreen()
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(text = item.title) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MyBlack,
                    selectedTextColor   = MyBlack,
                    unselectedIconColor = MyBlack,
                    unselectedTextColor = MyBlack,
                    indicatorColor      = MyPink
                )
            )
        }
    }
}