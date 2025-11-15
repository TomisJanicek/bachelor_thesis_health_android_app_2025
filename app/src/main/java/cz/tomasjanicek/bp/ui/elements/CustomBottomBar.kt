package cz.tomasjanicek.bp.ui.elements

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.SelectedContent
import cz.tomasjanicek.bp.ui.theme.UnselectedContent

data class BottomNavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomBar(
    navigationRouter: INavigationRouter,
    currentScreenIndex: Int
) {
    val items = listOf(
        BottomNavigationItem("Prohlídky",   Icons.Filled.List),
        BottomNavigationItem("Měření",      Icons.Filled.MonitorHeart),
        BottomNavigationItem("Léky",        Icons.Filled.Medication),
        BottomNavigationItem("Statistiky",  Icons.Filled.BarChart),
        BottomNavigationItem("Demo",  Icons.Filled.Alarm) //Připomínky
    )

    var selectedItemIndex by rememberSaveable { mutableStateOf(currentScreenIndex) }

    LaunchedEffect(currentScreenIndex) {
        selectedItemIndex = currentScreenIndex
    }

    NavigationBar(
        modifier = Modifier.padding(0.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    when (index) {
                        0 -> navigationRouter.navigaTetoListOfExaminationView()
                        4 -> navigationRouter.navigateToDemoScreen()
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
                    selectedIconColor   = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor   = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor      = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}