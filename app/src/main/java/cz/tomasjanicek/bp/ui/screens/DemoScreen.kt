package cz.tomasjanicek.bp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.ChartPoint
import cz.tomasjanicek.bp.ui.elements.CustomBottomBar
import cz.tomasjanicek.bp.ui.elements.LineChartWithControlsDemo

// ---------- Hlavní screen: graf + filtry + spodní menu ----------

@Composable
fun DemoScreen(
    navigationRouter: INavigationRouter,
    currentScreenIndex: Int
) {
    val now = System.currentTimeMillis()
    val dayMs = 86_400_000L
    val data = remember {
        (0..60).map { i ->
            val t = now - (60 - i) * dayMs
            val v = 120f + (Math.sin(i / 6.0) * 10 + Math.random() * 6).toFloat()
            ChartPoint(t, v)
        }
    }

    Scaffold(
        bottomBar = {
            CustomBottomBar(
                navigationRouter = navigationRouter,
                currentScreenIndex = currentScreenIndex
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 🟣 GRAF
            LineChartWithControlsDemo(allPoints = data)

            Spacer(modifier = Modifier.height(24.dp))

            // 🟢 FILTRAČNÍ SEKCE (dropdown + chips + seznam)
            FilterSection(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ---------- Modely pro filtry ----------

data class FilterOption(
    val id: String,
    val label: String,
)

data class Item(
    val id: Int,
    val name: String,
    val tags: List<String>,
)

// ---------- Společná filtrační sekce ----------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSection(
    modifier: Modifier = Modifier
) {
    // demo filtry
    val options = remember {
        listOf(
            FilterOption("android", "Android"),
            FilterOption("ios", "iOS"),
            FilterOption("web", "Web"),
            FilterOption("backend", "Backend"),
        )
    }

    // demo položky
    val items = remember {
        listOf(
            Item(1, "Android app", listOf("android")),
            Item(2, "iOS app", listOf("ios")),
            Item(3, "Fullstack web", listOf("web", "backend")),
            Item(4, "Backend API", listOf("backend")),
            Item(5, "Cross-platform", listOf("android", "ios", "web")),
        )
    }

    var selectedOptionIds by remember { mutableStateOf(setOf<String>()) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val filteredItems = if (selectedOptionIds.isEmpty()) {
        items
    } else {
        items.filter { item ->
            item.tags.any { it in selectedOptionIds }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ) {

        // 🔽 Tlačítko pro otevření multi-select menu
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { isMenuExpanded = true }
            ) {
                val count = selectedOptionIds.size
                Text(
                    text = if (count == 0)
                        "Vybrat filtry"
                    else
                        "Filtry ($count)"
                )
            }

            if (selectedOptionIds.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { selectedOptionIds = emptySet() }) {
                    Text("Vymazat")
                }
            }
        }

        // 🔽 Dropdown menu s checkboxy
        Box {
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false }
            ) {
                Text(
                    text = "Vyber filtry",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(12.dp)
                )

                options.forEach { option ->
                    val checked = option.id in selectedOptionIds
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = null // řeším přes onClick
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(option.label)
                            }
                        },
                        onClick = {
                            selectedOptionIds = selectedOptionIds.toMutableSet().apply {
                                if (contains(option.id)) remove(option.id) else add(option.id)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🏷️ Aktivní filtry jako chips
        if (selectedOptionIds.isNotEmpty()) {
            Text(
                text = "Aktivní filtry:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options
                    .filter { it.id in selectedOptionIds }
                    .forEach { option ->
                        FilterChip(
                            selected = true,
                            onClick = {
                                // kliknutím chip odebereme filtr
                                selectedOptionIds = selectedOptionIds.toMutableSet().apply {
                                    remove(option.id)
                                }
                            },
                            label = {
                                Text("× ${option.label}")
                            }
                        )
                    }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 📋 Výsledný seznam
        Text(
            text = "Výsledky (${filteredItems.size}):",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems) { item ->
                ItemRowDemo(item = item)
            }
        }
    }
}

// ---------- Původní FilterDemoScreen (pokud ho chceš zvlášť) ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDemoScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filtrovaný seznam – demo") }
            )
        }
    ) { padding ->
        FilterSection(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        )
    }
}

// ---------- Řádek položky ----------

@Composable
private fun ItemRowDemo(item: Item) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tagy: ${item.tags.joinToString()}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ---------- Preview ----------

@Preview(
    name = "Line Chart + Filters Demo",
    showBackground = true,
    backgroundColor = 0xFFFDFDFD
)
@Composable
fun PreviewLineChartDemo() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface {
            val mockRouter = object : INavigationRouter {
                override fun navigateToDemoScreen() { }
            }

            DemoScreen(
                navigationRouter = mockRouter,
                currentScreenIndex = 0
            )
        }
    }
}
