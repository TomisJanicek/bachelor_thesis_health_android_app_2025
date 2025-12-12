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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import cz.tomasjanicek.bp.ui.elements.ChartPeriod
import cz.tomasjanicek.bp.ui.elements.ChartPoint
import cz.tomasjanicek.bp.ui.elements.LineChart
import cz.tomasjanicek.bp.ui.elements.PeriodSelector
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyWhite
import cz.tomasjanicek.bp.utils.DateUtils
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementCategoryDetailScreen(
    navigationRouter: INavigationRouter,
    categoryId: Long,
    viewModel: MeasurementCategoryDetailViewModel = hiltViewModel()
) {    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.load(categoryId)
    }

    // Stav pro zobrazení dialogu s nápovědou
    var showHelpDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // --- ZDE ZAČÍNÁ DIALOG S NÁPOVĚDOU ---
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Nápověda k zobrazení grafu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Aby byl graf vždy co nejpřehlednější, automaticky upravuje podrobnost zobrazených dat podle zvoleného období.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Column {
                        Text(
                            "• Den a Týden:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "U krátkých období vidíte každý jednotlivý záznam jako samostatný bod.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Column {
                        Text(
                            "• 30 dní a Rok:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "U delších období se data seskupují (agregují) a graf ukazuje jejich průměrné hodnoty (týdenní nebo měsíční). Díky tomu vidíte dlouhodobý trend, aniž by byl graf nepřehledný.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Rozumím")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MediumTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
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
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                // --- PŘIDANÁ AKCE S OTAZNÍKEM ---
                actions = {
                    if (state is MeasurementCategoryDetailUIState.Content && (state as MeasurementCategoryDetailUIState.Content).measurements.isNotEmpty()) {
                        IconButton(onClick = { showHelpDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.HelpOutline,
                                contentDescription = "Nápověda k grafu",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    IconButton(onClick = {
                        // Zavoláme router s ID aktuální kategorie
                        navigationRouter.navigateToAddEditMeasurementCategory(categoryId)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Upravit kategorii",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
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
                    contentDescription = "Přidat měření"
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
                    Text("Kategorii se nepodařilo načíst.", color = MaterialTheme.colorScheme.error)
                }
            }

            is MeasurementCategoryDetailUIState.Content -> {
                // Lokální stav pro graf (který parametr a období jsou vybrány)
                var selectedFieldIdForChart by remember { mutableStateOf(currentState.fields.firstOrNull()?.id) }
                var selectedPeriodForChart by remember { mutableStateOf(ChartPeriod.DAYS_30) }

                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        MeasurementChartSection(
                            content = currentState,
                            selectedFieldId = selectedFieldIdForChart,
                            selectedPeriod = selectedPeriodForChart,
                            onFieldSelected = { selectedFieldIdForChart = it },
                            onPeriodSelected = { selectedPeriodForChart = it }
                        )
                    }

                    // --- SEZNAM ZÁZNAMŮ ---
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
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    "Přidejte své první měření pomocí tlačítka (+)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementChartSection(
    content: MeasurementCategoryDetailUIState.Content,
    selectedFieldId: Long?,
    selectedPeriod: ChartPeriod,
    onFieldSelected: (Long) -> Unit,
    onPeriodSelected: (ChartPeriod) -> Unit
) {
    // Zobrazíme jen pokud máme co (alespoň 1 pole a nějaká měření)
    if (content.fields.isEmpty() || content.measurements.isEmpty() || selectedFieldId == null) {
        return // Pokud není co zobrazit, sekci přeskočíme
    }

    val selectedField = content.fields.firstOrNull { it.id == selectedFieldId } ?: return

    // Vytvoříme body pro graf ze všech naměřených hodnot daného parametru
    val chartPoints = remember(content.valuesByMeasurementId, selectedFieldId, content.measurements) {
        val measurementTimes = content.measurements.associateBy({ it.id }, { it.measuredAt })
        content.valuesByMeasurementId.values.asSequence().flatten()
            .filter { it.fieldId == selectedFieldId }
            .mapNotNull { value ->
                measurementTimes[value.measurementId]?.let { time ->
                    ChartPoint(xEpochMillis = time, y = value.value.toFloat())
                }
            }
            .sortedBy { it.xEpochMillis }
            .toList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Trend hodnot",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        // --- VÝBĚR PARAMETRU (ExposedDropdownMenu) ---
        var isDropdownExpanded by remember { mutableStateOf(false) }

        if (content.fields.size > 1) { // Zobrazíme výběr jen pokud je z čeho vybírat
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedField.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Zobrazený parametr") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    content.fields.forEach { field ->
                        DropdownMenuItem(
                            text = { Text(field.label) },
                            onClick = {
                                onFieldSelected(field.id)
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }


        // --- VÝBĚR OBDOBÍ ---
        PeriodSelector(
            selected = selectedPeriod,
            onSelected = onPeriodSelected,
            modifier = Modifier.fillMaxWidth()
        )

        // --- SAMOTNÝ GRAF ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            LineChart(
                points = chartPoints,
                period = selectedPeriod,
                yLimitMin = selectedField.minValue?.toFloat(),
                yLimitMax = selectedField.maxValue?.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Divider(modifier = Modifier.padding(top = 16.dp))
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
            containerColor = MyGreen.copy(alpha = 0.2f),
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
                        color = MaterialTheme.colorScheme.primary
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
                    tint = MaterialTheme.colorScheme.onBackground
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
                        Icon(Icons.Default.Edit, contentDescription = "Upravit", modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Upravit")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onDeleteClick) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Smazat", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
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
 * Přidáno fieldId pro snazší filtrování v grafu.
 */
data class MeasurementValueDisplay(
    val fieldId: Long,
    val measurementId: Long,
    val label: String,      // např. "Systolický tlak"
    val unit: String? = null, // např. "mmHg"
    val value: Double       // např. "120"
)