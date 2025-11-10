package cz.tomasjanicek.bp.ui.elements

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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

data class BottomNavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// můžeš si pak přebarvit do svého theme
private val BottomBarBackground = Color(0xFF6E8283)   // zelenošedé pozadí
private val SelectedPillColor  = Color(0xFFE6B8B8)   // růžový indikátor
private val SelectedContent    = Color(0xFF000000)   // černý text/ikona vybraného
private val UnselectedContent  = Color(0xDD000000)   // lehce zeslabený pro nevybrané

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
        BottomNavigationItem("Připomínky",  Icons.Filled.Alarm)
    )

    var selectedItemIndex by rememberSaveable { mutableStateOf(currentScreenIndex) }

    LaunchedEffect(currentScreenIndex) {
        selectedItemIndex = currentScreenIndex
    }

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = BottomBarBackground,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0)
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    // tady si pak rozsekáš na konkrétní screeny
                    navigationRouter.navigateToDemoScreen()
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
                    selectedIconColor   = SelectedContent,
                    selectedTextColor   = SelectedContent,
                    unselectedIconColor = UnselectedContent,
                    unselectedTextColor = UnselectedContent,
                    indicatorColor      = SelectedPillColor
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun CustomBottomBarPreview() {
    val mockRouter = object : INavigationRouter {
        override fun navigateToDemoScreen() { }
    }

    CustomBottomBar(
        navigationRouter = mockRouter,
        currentScreenIndex = 0
    )
}
