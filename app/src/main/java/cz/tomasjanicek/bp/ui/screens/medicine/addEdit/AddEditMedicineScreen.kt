package cz.tomasjanicek.bp.ui.screens.medicine.addEdit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.values
import androidx.hilt.navigation.compose.hiltViewModel
import cz.tomasjanicek.bp.model.EndingType
import cz.tomasjanicek.bp.model.MedicineUnit
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.CustomDatePickerDialog
import cz.tomasjanicek.bp.ui.elements.CustomTimePickerDialog
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyRed
import cz.tomasjanicek.bp.ui.theme.MyWhite
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditMedicineScreen(
    navigationRouter: INavigationRouter,
    medicineId: Long?,
    viewModel: AddEditMedicineViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Stavy pro dialogy
    var showRegularTimePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) } // Nový dialog pro koncové datum
    var showSingleDatePicker by remember { mutableStateOf(false) }
    var showSingleTimePicker by remember { mutableStateOf(false) }
    var tempSelectedDate by remember { mutableStateOf(System.currentTimeMillis()) }


    // Stavy pro pickery
    val startDatePickerState = rememberDatePickerState(
        yearRange = (LocalDate.now().year - 1)..(LocalDate.now().year + 5),
        initialSelectedDateMillis = state.startDate
    )
    val endDatePickerState = rememberDatePickerState(
        yearRange = (LocalDate.now().year)..(LocalDate.now().year + 10),
        initialSelectedDateMillis = state.endDate
    )
    val singleDatePickerState =
        rememberDatePickerState(yearRange = (LocalDate.now().year)..(LocalDate.now().year + 5))
    val timePickerState = rememberTimePickerState(is24Hour = true)


    LaunchedEffect(medicineId) {
        viewModel.loadMedicine(medicineId)
    }

    // --- DIALOGY ---

    if (showRegularTimePicker) {
        CustomTimePickerDialog(
            timePickerState = timePickerState,
            onDismiss = { showRegularTimePicker = false },
            onConfirm = {
                val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                viewModel.onAction(AddEditMedicineAction.OnTimeAdded(selectedTime))
                showRegularTimePicker = false
            }
        )
    }

    if (showStartDatePicker) {
        CustomDatePickerDialog(
            datePickerState = startDatePickerState,
            onDismiss = { showStartDatePicker = false },
            onConfirm = {
                startDatePickerState.selectedDateMillis?.let {
                    viewModel.onAction(AddEditMedicineAction.OnStartDateChanged(it))
                }
                showStartDatePicker = false
                if (state.regularTimes.isEmpty()) {
                    showRegularTimePicker = true
                }
            }
        )
    }

    // Nový DatePicker pro koncové datum
    if (showEndDatePicker) {
        CustomDatePickerDialog(
            datePickerState = endDatePickerState,
            onDismiss = { showEndDatePicker = false },
            onConfirm = {
                endDatePickerState.selectedDateMillis?.let {
                    viewModel.onAction(AddEditMedicineAction.OnEndDateChanged(it))
                }
                showEndDatePicker = false
            }
        )
    }

    if (showSingleDatePicker) {
        CustomDatePickerDialog(
            datePickerState = singleDatePickerState,
            onDismiss = { showSingleDatePicker = false },
            onConfirm = {
                singleDatePickerState.selectedDateMillis?.let { tempSelectedDate = it }
                showSingleDatePicker = false
                showSingleTimePicker = true
            }
        )
    }

    if (showSingleTimePicker) {
        CustomTimePickerDialog(
            timePickerState = timePickerState,
            onDismiss = { showSingleTimePicker = false },
            onConfirm = {
                val date = Instant.ofEpochMilli(tempSelectedDate).atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                val finalDateTime =
                    date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                viewModel.onAction(AddEditMedicineAction.OnSingleDateAdded(finalDateTime))
                showSingleTimePicker = false
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditing) "Upravit lék" else "Nový lék",
                        color = MyBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.returBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zpět",
                            tint = MyBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyWhite)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MyWhite)
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.onAction(AddEditMedicineAction.OnSaveClicked)
                        if (viewModel.uiState.value.canBeSaved) {
                            navigationRouter.returBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Uložit")
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MyWhite)
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Snížená mezera
            ) {
                // --- ZÁKLADNÍ INFORMACE ---
                item { FormSection("Základní informace") }
                item {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.onAction(AddEditMedicineAction.OnNameChanged(it)) },
                        label = { Text("Název léku*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = state.name.isBlank() && state.hasAttemptedSave
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        OutlinedTextField(
                            value = state.dosage,
                            onValueChange = {
                                viewModel.onAction(
                                    AddEditMedicineAction.OnDosageChanged(
                                        it
                                    )
                                )
                            },
                            label = { Text("Dávka*") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = (state.dosage.toDoubleOrNull()
                                ?: 0.0) <= 0.0 && state.hasAttemptedSave
                        )
                        Spacer(Modifier.width(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = state.unit.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jednotka") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                MedicineUnit.values().forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.label, color = MyBlack) },
                                        onClick = {
                                            viewModel.onAction(
                                                AddEditMedicineAction.OnUnitSelected(
                                                    unit
                                                )
                                            )
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = { viewModel.onAction(AddEditMedicineAction.OnNoteChanged(it)) },
                        label = { Text("Poznámka (např. 'po jídle')") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // --- PŘIPOMÍNÁNÍ ---
                item { FormSection("Připomínání") }
                item {
                    RegularitySwitch(
                        isRegular = state.isRegular,
                        onCheckedChange = {
                            viewModel.onAction(
                                AddEditMedicineAction.OnRegularityChanged(
                                    it
                                )
                            )
                        }
                    )
                }

                if (state.isRegular) {
                    // --- PRAVIDELNÉ PŘIPOMÍNÁNÍ ---
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DateChip(
                                label = "Užívat od:",
                                dateMillis = state.startDate,
                                onClick = { showStartDatePicker = true },
                            )
                            DaySelector(
                                selectedDays = state.regularDays,
                                onDayClick = {
                                    viewModel.onAction(
                                        AddEditMedicineAction.OnDayOfWeekToggled(
                                            it
                                        )
                                    )
                                }
                            )
                            TimeSelector(
                                label = "V časy:*",
                                times = state.regularTimes,
                                onAddTimeClick = { showRegularTimePicker = true },
                                onRemoveTimeClick = {
                                    viewModel.onAction(
                                        AddEditMedicineAction.OnTimeRemoved(
                                            it
                                        )
                                    )
                                },
                                isError = state.regularTimes.isEmpty() && state.hasAttemptedSave
                            )
                        }
                    }
                    item {
                        EndingConditionSelector(
                            state = state,
                            onAction = viewModel::onAction,
                            onEndDateClick = { showEndDatePicker = true }
                        )
                    }
                } else {
                    // --- JEDNORÁZOVÉ PŘIPOMÍNÁNÍ ---
                    item {
                        DateTimeSelector(
                            label = "Jednorázové připomínky:*",
                            dateTimes = state.singleDates,
                            onAddDateTimeClick = { showSingleDatePicker = true },
                            onRemoveDateTimeClick = {
                                viewModel.onAction(
                                    AddEditMedicineAction.OnSingleDateRemoved(
                                        it
                                    )
                                )
                            },
                            isError = state.singleDates.isEmpty() && state.hasAttemptedSave
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MyBlack,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun RegularitySwitch(isRegular: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Pravidelné užívání", modifier = Modifier.weight(1f), color = MyBlack)
        Switch(checked = isRegular, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DaySelector(selectedDays: Set<DayOfWeek>, onDayClick: (DayOfWeek) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        DayOfWeek.values().forEach { day ->
            val isSelected = selectedDays.contains(day)
            FilterChip(
                selected = isSelected,
                onClick = { onDayClick(day) },
                label = { Text(day.getDisplayName(TextStyle.SHORT, Locale("cs", "CZ"))) },
                modifier = Modifier.height(36.dp),
                colors = FilterChipDefaults.filterChipColors(
                    disabledContainerColor = MyWhite,
                    disabledLabelColor = MyBlack,
                    selectedContainerColor = MyPink,
                    selectedLabelColor = MyBlack
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TimeSelector(
    label: String,
    times: Set<LocalTime>,
    onAddTimeClick: () -> Unit,
    onRemoveTimeClick: (LocalTime) -> Unit,
    isError: Boolean
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isError) MaterialTheme.colorScheme.error else MyBlack
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            times.sorted().forEach { time ->
                InputChip(
                    selected = false,
                    onClick = { /* Úprava času */ },
                    label = { Text(time.format(DateTimeFormatter.ofPattern("HH:mm"))) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Odebrat čas",
                            modifier = Modifier
                                .size(InputChipDefaults.IconSize)
                                .clickable { onRemoveTimeClick(time) }
                        )
                    }
                )
            }
            IconButton(onClick = onAddTimeClick) {
                Icon(Icons.Default.Add, contentDescription = "Přidat čas", tint = MyBlack)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateChip(label: String, dateMillis: Long, onClick: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("d. MMMM yyyy") }
    val dateText = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(formatter)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.padding(end = 8.dp), color = MyBlack)
        InputChip(
            selected = true,
            onClick = onClick,
            label = { Text(dateText, fontSize = 14.sp) },
            colors = InputChipDefaults.inputChipColors(
                disabledContainerColor = MyWhite,
                selectedContainerColor = MyPink,
                selectedLabelColor = MyBlack,
                selectedLeadingIconColor = MyBlack,
                trailingIconColor = MyBlack
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DateTimeSelector(
    label: String,
    dateTimes: Set<Long>,
    onAddDateTimeClick: () -> Unit,
    onRemoveDateTimeClick: (Long) -> Unit,
    isError: Boolean
) {
    val formatter = remember { DateTimeFormatter.ofPattern("d.M.yy HH:mm") }
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isError) MaterialTheme.colorScheme.error else MyBlack
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dateTimes.sorted().forEach { dateTimeMillis ->
                val dateTimeText =
                    Instant.ofEpochMilli(dateTimeMillis).atZone(ZoneId.systemDefault())
                        .format(formatter)
                InputChip(
                    selected = false,
                    onClick = { /* Úprava termínu */ },
                    label = { Text(dateTimeText, color = MyBlack) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Odebrat termín",
                            modifier = Modifier
                                .size(InputChipDefaults.IconSize)
                                .clickable { onRemoveDateTimeClick(dateTimeMillis) }
                        )
                    }
                )
            }
            IconButton(onClick = onAddDateTimeClick) {
                Icon(Icons.Default.Add, contentDescription = "Přidat termín", tint = MyBlack)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndingConditionSelector(
    state: AddEditMedicineUIState,
    onAction: (AddEditMedicineAction) -> Unit,
    onEndDateClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FormSection("Ukončení užívání")

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            EndingType.values().forEachIndexed { index, type ->
                SegmentedButton(
                    // Parametr 'shape' se bere přímo z 'SegmentedButtonDefaults'
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = EndingType.values().size
                    ),
                    onClick = { onAction(AddEditMedicineAction.OnEndingTypeChanged(type)) },
                    selected = state.endingType == type,
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MyPink,
                        activeContentColor = MyBlack, // Barva textu na aktivním
                        activeBorderColor = MyBlack,

                        inactiveContainerColor = MyWhite,
                        inactiveContentColor = MyBlack,
                        inactiveBorderColor = MyBlack
                    )
                ) {
                    Text(type.label)
                }
            }
        }

        when (state.endingType) {
            EndingType.UNTIL_DATE -> {
                DateChip(
                    label = "Do data:",
                    dateMillis = state.endDate
                        ?: state.startDate, // Zobrazí startovní datum, pokud koncové není
                    onClick = onEndDateClick
                )
            }

            EndingType.AFTER_DOSES -> {
                OutlinedTextField(
                    value = state.doseCount,
                    onValueChange = { onAction(AddEditMedicineAction.OnDoseCountChanged(it)) },
                    label = { Text("Počet dávek*") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = (state.doseCount.toIntOrNull() ?: 0) <= 0 && state.hasAttemptedSave
                )
            }

            EndingType.INDEFINITELY -> {
                // Nic se nezobrazuje
            }
        }
    }
}