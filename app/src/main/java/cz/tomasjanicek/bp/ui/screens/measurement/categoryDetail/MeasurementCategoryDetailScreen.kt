package cz.tomasjanicek.bp.ui.screens.measurement.categoryDetail

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.CustomBottomBar
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyWhite
import cz.tomasjanicek.bp.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementCategoryDetailScreen(
    navigationRouter: INavigationRouter,
    categoryId: Long,
    viewModel: MeasurementCategoryDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.load(categoryId)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier,
        containerColor = MyWhite,
        topBar = {
            MediumTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MyWhite,
                    scrolledContainerColor = MyWhite,
                    titleContentColor = MyBlack,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    val title = when (state) {
                        is MeasurementCategoryDetailUIState.Content ->
                            (state as MeasurementCategoryDetailUIState.Content).category.name

                        else -> "Načítání..."
                    }
                    Text(
                        title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.returBack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Zpět",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    // Zde může být v budoucnu ikona pro úpravu kategorie
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigationRouter.navigateToAddEditMeasurement(
                        categoryId = categoryId,
                        measurementId = null
                    )
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MyBlack
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add"
                )
            }
        }
    ) { innerPadding ->
        when (val currentState = state) {
            MeasurementCategoryDetailUIState.Loading -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }


            MeasurementCategoryDetailUIState.Error -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Kategorii se nepodařilo načíst.")
                }
            }

            is MeasurementCategoryDetailUIState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(top = 8.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentState.measurements.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Zatím nemáte žádné záznamy.",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Přidejte své první měření pomocí tlačítka (+)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(currentState.measurements, key = { it.id }) { measurement ->
                        val valuesForThisItem = currentState.valuesByMeasurementId[measurement.id].orEmpty()

                        MeasurementListItem(
                            measurement = measurement,
                            categoryName = currentState.category.name,
                            values = valuesForThisItem,
                            onEditClick = {
                                navigationRouter.navigateToAddEditMeasurement(
                                    categoryId = categoryId,
                                    measurementId = measurement.id
                                )
                            },
                            onDeleteClick = {
                                viewModel.deleteMeasurement(measurement)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

/**
 * Zobrazuje jednu položku v seznamu měření s rozbalovacím detailem.
 */
@Composable
private fun MeasurementListItem(
    measurement: Measurement,
    categoryName: String,
    values: List<MeasurementValueDisplay>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(animationSpec = spring()), // Plynulá animace
        colors = CardDefaults.cardColors(
            containerColor = MyGreen.copy(alpha = 0.8f),
            contentColor = MyBlack
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- VŽDY VIDITELNÁ ČÁST ---
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Malý "tag" s názvem kategorie
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MyBlack
                    )
                    // Hlavní text – datum a čas
                    Text(
                        text = DateUtils.getDateTimeString(measurement.measuredAt),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Šipka pro indikaci stavu rozbalení
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Rozbalit/Sbalit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- ROZBALOVACÍ ČÁST (zobrazí se po kliknutí) ---
            if (isExpanded) {
                Divider(color = MyBlack.copy(alpha = 0.1f))

                // Detailní seznam naměřených hodnot
                if (values.isNotEmpty()) {
                    Text(
                        text = "Naměřené hodnoty",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        values.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                val valueText = buildString {
                                    append(item.value)
                                    if (!item.unit.isNullOrBlank()) {
                                        append(" ")
                                        append(item.unit)
                                    }
                                }
                                Text(
                                    text = valueText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Akční tlačítka
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Upravit",
                            tint = MyBlack,
                            modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Upravit", color = MyBlack)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onDeleteClick) {
                        Icon(Icons.Outlined.Delete,
                            contentDescription = "Smazat",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Smazat", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

/**
 * UI model pro jednu naměřenou hodnotu.
 */
data class MeasurementValueDisplay(
    val label: String,      // např. "Systolický tlak"
    val unit: String? = null, // např. "mmHg"
    val value: Double       // např. "120"
)