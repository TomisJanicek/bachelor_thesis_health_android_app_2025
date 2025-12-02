package cz.tomasjanicek.bp.ui.screens.examination.addEdit

import android.text.format.DateUtils.formatDateTime
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.values
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tomasjanicek.bp.model.ExaminationType
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyWhite
import cz.tomasjanicek.bp.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExaminationScreen(
    navigationRouter: INavigationRouter,
    id: Long?
) {
    val viewModel = hiltViewModel<AddEditExaminationViewModel>()

    val state = viewModel.addEditExaminationUIState.collectAsStateWithLifecycle()

    var data by remember {
        mutableStateOf(AddEditExaminationData())
    }

    state.value.let {
        when (it) {
            AddEditExaminationUIState.Loading -> {
                viewModel.loadExamination(id)
            }

            is AddEditExaminationUIState.ExaminationChanged -> {
                data = it.data
            }

            AddEditExaminationUIState.ExaminationDeleted -> {
                navigationRouter.returBack()
            }

            AddEditExaminationUIState.ExaminationSaved -> {
                navigationRouter.returBack()
            }
        }
    }
    Scaffold(
        containerColor = MyWhite,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    // Použij primární barvu z tvého tématu (která je MyGreen)
                    containerColor = MyWhite,
                    scrolledContainerColor = MyWhite,

                    // Barva pro nadpis a ikony
                    titleContentColor = MyBlack,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Text("Přidat /upravit")
                },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.returBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Zpět",
                            tint = Color.Black // Zpětná šipka černá
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteExamination()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Odstranit"
                        )
                    }
                },
            )
        },
        // Tlačítko pro uložení
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MyWhite
            ) {
                Button(
                    onClick = { viewModel.saveExamination() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    // Tlačítko bude aktivní, jen pokud je vyplněn účel
                    enabled = data.purposeError == null
                ) {
                    Text("Uložit")
                }
            }
        }
    ) { innerPadding ->
        AddEditExaminationContent(
            modifier = Modifier.padding(innerPadding),
            data = data,
            actions = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExaminationContent(
    modifier: Modifier = Modifier,
    data: AddEditExaminationData,
    actions: AddEditExaminationAction
) {
    // 1. Stavy pro zobrazení/skrytí dialogových oken
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var doctorMenuExpanded by remember { mutableStateOf(false) }

    // 2. Stavy pro samotný DatePicker a TimePicker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = data.examination.dateTime
    )
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = data.examination.dateTime }
            .get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = data.examination.dateTime }
            .get(Calendar.MINUTE),
        is24Hour = true
    )

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // --- POLE PRO ÚČEL ---
        item {
            OutlinedTextField(
                value = data.examination.purpose,
                onValueChange = { actions.onPurposeChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Účel vyšetření") },
                isError = data.purposeError != null,
                singleLine = true
            )
        }
        item {
            val selectedDoctorName =
                data.doctors.find { it.id == data.examination.doctorId }?.specialization ?: ""
            ExposedDropdownMenuBox(
                expanded = doctorMenuExpanded,
                onExpandedChange = { doctorMenuExpanded = !doctorMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedDoctorName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lékař") },
                    placeholder = { Text("Vyberte lékaře") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorMenuExpanded) },
                    isError = data.doctorError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor( // <-- Nové, správné volání
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true // Pole je aktivní pro rozbalení menu
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        // --- Normální stav (není vybráno, není chyba) ---
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,

                        // --- Stav, když je pole vybráno (kliknuto) ---
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                    )
                )
                ExposedDropdownMenu(
                    expanded = doctorMenuExpanded,
                    onDismissRequest = { doctorMenuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.onBackground) // Nebo jakákoliv jiná barva
                ) {
                    data.doctors.forEach { doctor ->
                        DropdownMenuItem(
                            // Zobrazíme jméno i specializaci
                            text = { Text("${doctor.name} (${doctor.specialization})") },
                            onClick = {
                                actions.onDoctorChanged(doctor.id)
                                doctorMenuExpanded = false
                            },

                            // ZDE SE NASTAVUJÍ BARVY PRO POLOŽKU V MENU

                            colors = MenuDefaults.itemColors(
                                // Barva textu položky
                                textColor = MaterialTheme.colorScheme.onSurface,
                                // Barva ikony na začátku (pokud by byla)
                                leadingIconColor = MaterialTheme.colorScheme.onSurface,
                                // Barva ikony na konci (pokud by byla)
                                trailingIconColor = MaterialTheme.colorScheme.onSurface,
                                // Barva pro neaktivní (disabled) položku
                                disabledTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }

        // --- VÝBĚR TYPU VYŠETŘENÍ ---
        item {
            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
            ) {
                OutlinedTextField(
                    value = data.examination.type.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Typ vyšetření") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor( // <-- Nové, správné volání
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true // Pole je aktivní pro rozbalení menu
                        ), // Důležité pro správné umístění menu
                    colors = OutlinedTextFieldDefaults.colors(
                        // --- Normální stav (není vybráno, není chyba) ---
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,

                        // --- Stav, když je pole vybráno (kliknuto) ---
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                    )
                )
                ExposedDropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.onBackground) // Nebo jakákoliv jiná barva
                ) {
                    ExaminationType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = {
                                actions.onTypeChanged(type)
                                typeMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // --- VÝBĚR DATA A ČASU ---
        item {
            OutlinedTextField(
                // POUŽITÍ DateUtils
                value = DateUtils.getDateTimeString(data.examination.dateTime),
                onValueChange = { /* Read-only */ },
                label = { Text("Datum a čas vyšetření") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Datum") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .background(MaterialTheme.colorScheme.onBackground),

                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.onSurface,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }

        // --- POLE PRO POZNÁMKU ---
        item {
            OutlinedTextField(
                value = data.examination.note ?: "",
                onValueChange = { actions.onNoteChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Poznámka (nepovinné)") },
                minLines = 3
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        showTimePicker = true
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MyBlack)
                ) { Text("Vybrat čas") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MyBlack)
                )
                { Text("Zrušit") }
            },
            colors = DatePickerDefaults.colors(
                // 1. Nastavíme barvu pozadí dialogu na černou
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            DatePicker(
                state = datePickerState,
                // 3. Nezapomeňte nastavit barvy i pro vnitřní DatePicker!
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primary, // Pozadí samotného kalendáře
                    titleContentColor = MyBlack,
                    headlineContentColor = MyBlack,
                    weekdayContentColor = MyBlack,
                    subheadContentColor = MyBlack,
                    dayContentColor = MyBlack,
                    disabledDayContentColor = MyBlack.copy(alpha = 0.38f),
                    disabledSelectedDayContentColor = MyBlack.copy(alpha = 0.38f),
                    selectedDayContentColor = MyBlack,
                    selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                    todayContentColor = MyBlack,
                    todayDateBorderColor = MaterialTheme.colorScheme.secondary,
                    dividerColor = MyBlack.copy(alpha = 0.5f),
                    yearContentColor = MyBlack,
                    navigationContentColor = MyBlack,
                    selectedYearContentColor = MyBlack,
                    currentYearContentColor = MyBlack,


                    )
            )
        }
    }

    if (showTimePicker) {
        CustomTimePickerDialog( // Používáme vlastní Composable níže
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        val selectedDate = Calendar.getInstance().apply {
                            timeInMillis =
                                datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        }
                        selectedDate.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        selectedDate.set(Calendar.MINUTE, timePickerState.minute)
                        actions.onDateTimeChanged(selectedDate.timeInMillis)
                    }
                ) { Text("Potvrdit") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Zrušit") }
            }
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = MyPink,
                    clockDialSelectedContentColor = MyPink,
                    clockDialUnselectedContentColor = MyPink,
                    selectorColor = MyPink,
                    containerColor = MyPink,
                    //periodSelectorBorderColor = MyPink,
                    //periodSelectorSelectedContainerColor = MyPink,
                    //periodSelectorUnselectedContainerColor = MyPink,
                    //periodSelectorSelectedContentColor = MyPink,
                    //periodSelectorUnselectedContentColor = MyPink,
                    //timeSelectorSelectedContainerColor = MyPink,
                    timeSelectorUnselectedContainerColor = MyPink,
                    timeSelectorSelectedContentColor = MyPink,
                    timeSelectorUnselectedContentColor = MyPink,
                )
            )
        }
    }
}

/// Změnil jsem název, aby bylo jasné, že je to vlastní implementace
@Composable
private fun CustomTimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.padding(top = 24.dp, bottom = 12.dp)) {
                    content() // Zde je TimePicker
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, end = 6.dp), horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    confirmButton()
                }
            }
        }
    }
}