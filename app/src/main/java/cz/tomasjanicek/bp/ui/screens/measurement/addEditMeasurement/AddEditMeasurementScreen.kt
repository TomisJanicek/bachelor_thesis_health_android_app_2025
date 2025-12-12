package cz.tomasjanicek.bp.ui.screens.measurement.addEditMeasurement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.CustomDatePickerDialog
import cz.tomasjanicek.bp.ui.elements.CustomTimePickerDialog
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyWhite
import cz.tomasjanicek.bp.utils.DateUtils
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMeasurementScreen(
    navigationRouter: INavigationRouter,
    categoryId: Long,
    measurementId: Long?
) {
    val viewModel = hiltViewModel<AddEditMeasurementViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddEditMeasurementEvent.NavigateBack -> {
                    navigationRouter.returBack()
                }
            }
        }
    }
    LaunchedEffect(key1 = categoryId, key2 = measurementId) {
        viewModel.loadMeasurement(categoryId, measurementId)
    }

    val data = (state as? AddEditMeasurementUIState.MeasurementChanged)?.data

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Text(data?.category?.name ?: "Měření")
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
                    if (data?.measurement?.id != 0L) {
                        IconButton(onClick = { viewModel.deleteMeasurement() }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Odstranit měření",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
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
                    onClick = { viewModel.saveMeasurement() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = data != null
                ) {
                    Text("Uložit", color = MyBlack)
                }
            }
        }
    ) { innerPadding ->
        when (val currentState = state) {
            is AddEditMeasurementUIState.MeasurementChanged -> {
                AddEditMeasurementContent(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(WindowInsets.ime.asPaddingValues()),
                    data = currentState.data,
                    actions = viewModel
                )
            }
            AddEditMeasurementUIState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditMeasurementContent(
    modifier: Modifier = Modifier,
    data: AddEditMeasurementData,
    actions: AddEditMeasurementAction
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // --- OPRAVA ZDE ---
    val datePickerState = remember(data.measurement.measuredAt) {
        DatePickerState(
            // Povinný parametr, který chyběl
            locale = Locale.getDefault(),
            initialSelectedDateMillis = data.measurement.measuredAt
        )
    }

    val timePickerState = remember(data.measurement.measuredAt) {
        val cal = Calendar.getInstance().apply { timeInMillis = data.measurement.measuredAt }
        TimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
    }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Datum a čas
        item {
            OutlinedTextField(
                value = DateUtils.getDateTimeString(data.measurement.measuredAt),
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                label = { Text("Datum a čas měření") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Datum", tint = MaterialTheme.colorScheme.onBackground)
                },
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Hodnoty
        items(data.fields, key = { it.field.id }) { fieldUi ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = fieldUi.valueText,
                    onValueChange = { actions.onFieldValueChanged(fieldUi.field.id, it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            if (fieldUi.field.unit.isNullOrBlank())
                                fieldUi.field.label
                            else
                                "${fieldUi.field.label} (${fieldUi.field.unit})"
                        )
                    },
                    singleLine = true,
                    isError = fieldUi.error != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                fieldUi.error?.let {
                    Text(
                        text = stringResource(id = it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
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
                    // --- OPRAVA ZDE ---
                    timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                }
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                cal.set(Calendar.MINUTE, timePickerState.minute)
                actions.onDateTimeChanged(cal.timeInMillis)
            }
        )
    }
}