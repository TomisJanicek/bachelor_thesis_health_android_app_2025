package cz.tomasjanicek.bp.ui.screens.stats

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.ChartPeriod
import cz.tomasjanicek.bp.ui.elements.ChartPoint
import cz.tomasjanicek.bp.ui.elements.bottomBar.CustomBottomBar
import cz.tomasjanicek.bp.ui.elements.CustomDatePickerDialog
import cz.tomasjanicek.bp.ui.elements.LineChart
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyPink
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import android.content.Intent // <-- PŘIDAT IMPORT
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navigationRouter: INavigationRouter,
    viewModel: StatsViewModel = hiltViewModel(),
    currentScreenIndex: Int
) {
    val state by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(true) }
    val context = LocalContext.current // Získáme kontext pro zobrazení Toastu
    val exportedFileUri by viewModel.exportedFileUri.collectAsState()


    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Po návratu z dialogu (ať už uživatel něco udělal, nebo ne)
        // resetujeme URI ve ViewModelu, aby se dialog nespustil znovu
        // například při otočení obrazovky.
        viewModel.onExportHandled()
    }

    LaunchedEffect(exportedFileUri) {
        exportedFileUri?.let { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/pdf"
                // Důležité: Dáváme oprávnění cílové aplikaci (Gmail, Disk, ...)
                // ke čtení našeho souboru.
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // Vytvoříme "Chooser", který dá uživateli na výběr, kam soubor poslat
            val chooser = Intent.createChooser(shareIntent, "Sdílet PDF report...")
            shareLauncher.launch(chooser)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,

                    // Barva pro nadpis a ikony
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Text(
                        "Prohlídky",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Navigace na profil */ }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profil uživatele"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Navigace do nastavení */ }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Nastavení"
                        )
                    }
                },
            )
        },
        bottomBar = {
            CustomBottomBar(
                navigationRouter = navigationRouter,
                currentScreenIndex = currentScreenIndex
            )
        },
        floatingActionButton = {
            // Zobrazíme FAB jen pokud je co exportovat
            if (state.chartData.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // Tlačítko teď už jen spustí akci ve ViewModelu.
                        // Toast zprávu můžeme nechat jako zpětnou vazbu, že se něco děje.
                        Toast.makeText(context, "Generuji PDF report...", Toast.LENGTH_SHORT).show()
                        viewModel.onAction(StatsAction.OnExportClicked)
                    },
                    icon = { Icon(Icons.Default.Share, "Exportovat") },
                    text = { Text("Exportovat") },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MyBlack
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FilterSection(
                state = state,
                onAction = viewModel::onAction,
                isExpanded = showFilters,
                onToggleExpand = { showFilters = !showFilters }
            )

            // --- ZOBRAZENÍ GRAFŮ NEBO PRÁZDNÉHO STAVU ---
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.chartData.isEmpty()) {
                EmptyStateStats(
                    hasAnyCategory = state.allCategories.isNotEmpty(),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.chartData, key = { it.categoryWithFields.category.id }) { chartData ->
                        val (startDate, endDate) = if (state.selectedPeriodType == StatsPeriodType.CUSTOM) {
                            state.customStartDate to state.customEndDate
                        } else {
                            null to null
                        }
                        ChartCard(
                            chartData = chartData,
                            statsPeriodType = state.selectedPeriodType,
                            customStartDate = startDate,
                            customEndDate = endDate
                        )
                    }
                    // Mezera na konci pro FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    state: StatsState,
    onAction: (StatsAction) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .animateContentSize(animationSpec = spring()), // Tato animace zajistí plynulé rozbalení
        colors = CardDefaults.cardColors(containerColor = MyGreen.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // HORNÍ LIŠTA FILTRU
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtry", tint = MyBlack)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Filtry zobrazení",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    color = MyBlack
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Rozbalit/Sbalit filtry",
                    tint = MyBlack
                )
            }

            // Rozbalovací obsah filtrů
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring()) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring()) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    //verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Divider() // Vizuální oddělovač
                    Spacer(Modifier.height(8.dp))
                    CategorySelector(
                        allCategories = state.allCategories,
                        selectedIds = state.selectedCategoryIds,
                        onSelectionChanged = { id, isSelected ->
                            onAction(StatsAction.OnCategorySelectionChanged(id, isSelected))
                        }
                    )
                    PeriodTypeSelector(
                        selected = state.selectedPeriodType,
                        onSelected = { onAction(StatsAction.OnPeriodTypeChanged(it)) }
                    )
                    if (state.selectedPeriodType == StatsPeriodType.CUSTOM) {
                        CustomDateRangeSelector(
                            startDate = state.customStartDate,
                            endDate = state.customEndDate,
                            onStartDateChanged = { onAction(StatsAction.OnCustomStartDateChanged(it)) },
                            onEndDateChanged = { onAction(StatsAction.OnCustomEndDateChanged(it)) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelector(
    allCategories: List<MeasurementCategory>,
    selectedIds: Set<Long>,
    onSelectionChanged: (Long, Boolean) -> Unit
) {
    Column {
        Text("Zobrazit měření:", style = MaterialTheme.typography.titleMedium, color = MyBlack)
        Spacer(Modifier.height(8.dp))
        if (allCategories.isEmpty()) {
            Text(
                "Nemáte vytvořené žádné kategorie měření.",
                style = MaterialTheme.typography.bodyMedium,
                color = MyBlack
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allCategories.forEach { category ->
                    val isSelected = selectedIds.contains(category.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectionChanged(category.id, !isSelected) },
                        label = { Text(category.name) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, "Vybráno", Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MyGreen,
                            selectedLabelColor = MyBlack,
                            disabledContainerColor = MaterialTheme.colorScheme.background,
                            disabledLabelColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodTypeSelector(
    selected: StatsPeriodType,
    onSelected: (StatsPeriodType) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        StatsPeriodType.values().forEachIndexed { index, periodType ->
            SegmentedButton(
                selected = selected == periodType,
                onClick = { onSelected(periodType) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = StatsPeriodType.values().size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MyPink,
                    activeContentColor = MyBlack,
                    inactiveBorderColor = MyBlack.copy(alpha = 0.5f)
                )
            ) {
                Text(periodType.label)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDateRangeSelector(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChanged: (LocalDate) -> Unit,
    onEndDateChanged: (LocalDate) -> Unit,
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val formatter = remember { DateTimeFormatter.ofPattern("d. M. yyyy") }

    // Start Date Picker
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate.atStartOfDay(
            ZoneOffset.UTC).toInstant().toEpochMilli())
        CustomDatePickerDialog(
            datePickerState = datePickerState,
            onDismiss = { showStartDatePicker = false },
            onConfirm = {
                datePickerState.selectedDateMillis?.let {
                    onStartDateChanged(LocalDate.ofEpochDay(it / (1000 * 60 * 60 * 24)))
                }
                showStartDatePicker = false
            }
        )
    }

    // End Date Picker
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        CustomDatePickerDialog(
            datePickerState = datePickerState,
            onDismiss = { showEndDatePicker = false },
            onConfirm = {
                datePickerState.selectedDateMillis?.let {
                    onEndDateChanged(LocalDate.ofEpochDay(it / (1000 * 60 * 60 * 24)))
                }
                showEndDatePicker = false
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = { showStartDatePicker = true }, modifier = Modifier.weight(1f)) {
            Text("Od: ${startDate.format(formatter)}")
        }
        OutlinedButton(onClick = { showEndDatePicker = true }, modifier = Modifier.weight(1f)) {
            Text("Do: ${endDate.format(formatter)}")
        }
    }
}

@Composable
fun ChartCard(
    chartData: StatsChartData,
    statsPeriodType: StatsPeriodType,
    customStartDate: LocalDate?,
    customEndDate: LocalDate?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            // Název celé kategorie
            Text(
                text = chartData.categoryWithFields.category.name,
                style = MaterialTheme.typography.headlineSmall, // Větší nadpis
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Pokud nejsou definována žádná pole, zobrazíme zprávu
            if (chartData.categoryWithFields.fields.isEmpty()) {
                Text(
                    "V této kategorii nejsou definovány žádné měřitelné parametry.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                return@Column // Ukončíme Column
            }

            // Odsazení mezi nadpisem a prvním grafem
            Spacer(Modifier.height(16.dp))

            // --- Cyklus pro každý parametr (field) ---
            chartData.categoryWithFields.fields.forEach { field ->
                // Data pro graf jen pro tento konkrétní parametr
                val chartPoints = remember(chartData.measurementsWithValues, field.id) {
                    chartData.measurementsWithValues.mapNotNull { measurement ->
                        val value = measurement.values.find { it.categoryFieldId == field.id }
                        if (value != null) {
                            ChartPoint(xEpochMillis = measurement.measurement.measuredAt, y = value.value.toFloat())
                        } else null
                    }
                }

                // Název konkrétního parametru (grafu)
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))

                // Logika období pro `LineChart`
                val chartPeriod = when (statsPeriodType) {
                    StatsPeriodType.DAY -> ChartPeriod.DAY
                    StatsPeriodType.WEEK -> ChartPeriod.WEEK
                    StatsPeriodType.MONTH -> ChartPeriod.DAYS_30
                    StatsPeriodType.YEAR -> ChartPeriod.YEAR
                    StatsPeriodType.CUSTOM -> ChartPeriod.DAYS_30 // Použije se pro styl a případnou agregaci
                }

                LineChart(
                    points = chartPoints,
                    period = chartPeriod,
                    yLimitMin = field.minValue?.toFloat(),
                    yLimitMax = field.maxValue?.toFloat(),
                    customStartDate = customStartDate,
                    customEndDate = customEndDate
                )

                // Oddělovač mezi grafy v jedné kartě
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}



@Composable
private fun EmptyStateStats(hasAnyCategory: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (hasAnyCategory) "Vyberte filtry pro zobrazení statistik" else "Nejprve vytvořte kategorie měření",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (hasAnyCategory) "Zvolte alespoň jednu kategorii měření, pro kterou chcete zobrazit graf." else "Statistiky lze zobrazit až poté, co budete mít definované vlastní kategorie a v nich nějaká data.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}