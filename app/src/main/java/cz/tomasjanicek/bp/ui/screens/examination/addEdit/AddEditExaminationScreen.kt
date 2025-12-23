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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.ExaminationType
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.CustomDatePickerDialog
import cz.tomasjanicek.bp.ui.elements.CustomTimePickerDialog
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyRed
import cz.tomasjanicek.bp.utils.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExaminationScreen(
    navigationRouter: INavigationRouter,
    id: Long?
) {
    val viewModel = hiltViewModel<AddEditExaminationViewModel>()
    val state = viewModel.addEditExaminationUIState.collectAsStateWithLifecycle()

    // 1. Zjištění, zda upravujeme (ID není null a není 0/-1)
    val isEditMode = id != null && id != 0L && id != -1L

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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                // 2. Dynamický nadpis podle režimu
                title = {
                    Text(if (isEditMode) "Upravit prohlídku" else "Přidat prohlídku")
                },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.returBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Zpět",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // 3. Tlačítko pro smazání zobrazit POUZE v režimu úprav
                    if (isEditMode) {
                        IconButton(onClick = {
                            viewModel.deleteExamination()
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Odstranit",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { viewModel.saveExamination() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = data.purposeError == null && data.doctorError == null && data.dateTimeError == null
                ) {
                    Text("Uložit", color = MyBlack)
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
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var doctorMenuExpanded by remember { mutableStateOf(false) }

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
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                )
            )
            if (data.purposeError != null) {
                Text(
                    text = stringResource(id = data.purposeError),
                    color = MyRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        item {
            fun getDoctorDisplayName(doctor: Doctor): String {
                return if (!doctor.name.isNullOrBlank()) {
                    "${doctor.name} (${doctor.specialization})"
                } else {
                    doctor.specialization
                }
            }

            val selectedDoctor = data.doctors.find { it.id == data.examination.doctorId }
            val selectedDoctorText = selectedDoctor?.let { getDoctorDisplayName(it) } ?: ""
            val sortedDoctors = data.doctors.sortedBy { getDoctorDisplayName(it) }

            ExposedDropdownMenuBox(
                expanded = doctorMenuExpanded,
                onExpandedChange = { doctorMenuExpanded = !doctorMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedDoctorText,
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
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
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
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    sortedDoctors.forEach { doctor ->
                        DropdownMenuItem(
                            text = {
                                Text(text = getDoctorDisplayName(doctor))
                            },
                            onClick = {
                                actions.onDoctorChanged(doctor.id)
                                doctorMenuExpanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onBackground,
                                leadingIconColor = MaterialTheme.colorScheme.onBackground,
                                trailingIconColor = MaterialTheme.colorScheme.onBackground,
                                disabledTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
            if (data.doctorError != null) {
                Text(
                    text = stringResource(id = data.doctorError),
                    color = MyRed,
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
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
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
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
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
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Datum", tint = MaterialTheme.colorScheme.onBackground) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledBorderColor = MaterialTheme.colorScheme.onBackground,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onBackground,
                    disabledLabelColor = MaterialTheme.colorScheme.onBackground,
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
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
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
                    timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                }
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                cal.set(Calendar.MINUTE, timePickerState.minute)
                actions.onDateTimeChanged(cal.timeInMillis)
            }
        )
    }
}