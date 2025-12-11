package cz.tomasjanicek.bp.ui.screens.examination.addEdit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material3.DatePickerState
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
import androidx.compose.material3.TimePickerState
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
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.ExaminationType
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.CustomDatePickerDialog
import cz.tomasjanicek.bp.ui.elements.CustomTimePickerDialog
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyRed
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // <-- PŘIDAT TENTO ŘÁDEK
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
            // Použijeme standardní Row pro zarovnání a padding
            Row(modifier = Modifier
                .fillMaxWidth()
                // Barva pozadí stejná jako u TopAppBar pro konzistenci
                .background(MyWhite)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { viewModel.saveExamination() },
                    modifier = Modifier.fillMaxWidth(),
                    // Tlačítko bude aktivní, pouze pokud jsou všechna povinná pole validní
                    enabled = data.purposeError == null && data.doctorError == null && data.dateTimeError == null
                ) {
                    Text("Uložit")
                }
            }
        }
    ) { innerPadding ->
        AddEditExaminationContent(
            modifier = Modifier
                .padding(innerPadding)
                .padding(WindowInsets.ime.asPaddingValues()),
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
    val datePickerState = remember(data.examination.dateTime) {
        DatePickerState(
            locale = java.util.Locale.getDefault(),
            initialSelectedDateMillis = data.examination.dateTime
        )
    }

    val timePickerState = remember(data.examination.dateTime) {
        val cal = Calendar.getInstance().apply { timeInMillis = data.examination.dateTime }
        TimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
    }

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
            if (data.purposeError != null) {
                Text(
                    text = stringResource(id = data.purposeError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp) // Odsadíme, aby licoval s textem
                )
            }
        }
        item {
            // 1. Pomocná funkce pro formátování jména (aby se logika neopakovala)
            fun getDoctorDisplayName(doctor: Doctor): String {
                return if (!doctor.name.isNullOrBlank()) {
                    "${doctor.name} (${doctor.specialization})"
                } else {
                    doctor.specialization
                }
            }

            // 2. Získání textu pro vybraného lékaře (použijeme stejné formátování)
            val selectedDoctor = data.doctors.find { it.id == data.examination.doctorId }
            val selectedDoctorText = selectedDoctor?.let { getDoctorDisplayName(it) } ?: ""

            // 3. Seřazení seznamu lékařů abecedně podle zobrazovaného názvu
            val sortedDoctors = data.doctors.sortedBy { getDoctorDisplayName(it) }

            ExposedDropdownMenuBox(
                expanded = doctorMenuExpanded,
                onExpandedChange = { doctorMenuExpanded = !doctorMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedDoctorText, // Zde použijeme formátovaný text
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lékař") },
                    placeholder = { Text("Vyberte lékaře") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorMenuExpanded) },
                    isError = data.doctorError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    modifier = Modifier.background(MaterialTheme.colorScheme.onBackground)
                ) {
                    // ZMĚNA: Iterujeme přes seřazený seznam
                    sortedDoctors.forEach { doctor ->
                        DropdownMenuItem(
                            text = {
                                // Zde voláme naši formátovací funkci
                                Text(text = getDoctorDisplayName(doctor))
                            },
                            onClick = {
                                actions.onDoctorChanged(doctor.id)
                                doctorMenuExpanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onSurface,
                                leadingIconColor = MaterialTheme.colorScheme.onSurface,
                                trailingIconColor = MaterialTheme.colorScheme.onSurface,
                                disabledTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
            if (data.doctorError != null) {
                Text(
                    text = stringResource(id = data.doctorError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
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
                value = DateUtils.getDateTimeString(data.examination.dateTime),
                onValueChange = { /* Read-only */ },
                label = { Text("Datum a čas vyšetření") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Datum") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MyBlack,
                    disabledBorderColor = MyBlack,
                    disabledLeadingIconColor = MyBlack,
                    disabledLabelColor = MyBlack,
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
        CustomDatePickerDialog(
            datePickerState = datePickerState,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                showDatePicker = false
                showTimePicker = true
            }
        )
    }

    if (showTimePicker) {
        CustomTimePickerDialog(
            timePickerState = timePickerState,
            onDismiss = { showTimePicker = false },
            onConfirm = {
                showTimePicker = false
                val cal = Calendar.getInstance().apply {
                    // Použijeme správnou vlastnost `selectedDateMillis`
                    timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                }
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                cal.set(Calendar.MINUTE, timePickerState.minute)
                actions.onDateTimeChanged(cal.timeInMillis)
            }
        )
    }
}