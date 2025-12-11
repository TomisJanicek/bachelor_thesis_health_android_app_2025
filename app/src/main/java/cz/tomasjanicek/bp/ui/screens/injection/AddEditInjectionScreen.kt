package cz.tomasjanicek.bp.ui.screens.injection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.values
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tomasjanicek.bp.model.InjectionCategory
import cz.tomasjanicek.bp.model.PredefinedVaccine
import cz.tomasjanicek.bp.model.PredefinedVaccines
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.CustomDatePickerDialog
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyWhite
import cz.tomasjanicek.bp.utils.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditInjectionScreen(
    navigationRouter: INavigationRouter,
    injectionId: Long?
) {
    val viewModel = hiltViewModel<AddEditInjectionViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = injectionId) {
        viewModel.loadInjection(injectionId)
    }

    when (val state = uiState) {
        is AddEditInjectionUIState.Loading -> {
            // Můžete zde zobrazit progress bar
        }
        is AddEditInjectionUIState.Success -> {
            AddEditInjectionContent(
                navigationRouter = navigationRouter,
                data = state.data,
                actions = viewModel,
                isEditMode = injectionId != null && injectionId != 0L && injectionId != -1L
            )
        }
        is AddEditInjectionUIState.InjectionSaved -> {
            LaunchedEffect(Unit) {
                navigationRouter.returBack()
            }
        }
        is AddEditInjectionUIState.InjectionDeleted -> {
            LaunchedEffect(Unit) {
                navigationRouter.returBack()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditInjectionContent(
    navigationRouter: INavigationRouter,
    data: AddEditInjectionData,
    actions: AddEditInjectionAction,
    isEditMode: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showVaccinePickerDialog by remember { mutableStateOf(false) }

    val datePickerState = remember(data.injection.date) {
        DatePickerState(
            locale = java.util.Locale.getDefault(),
            initialSelectedDateMillis = data.injection.date
        )
    }

    if (showVaccinePickerDialog) {
        VaccinePickerDialog(
            onDismiss = { showVaccinePickerDialog = false },
            onVaccineSelected = { predefinedVaccine ->
                // OPRAVA: Nyní nastavujeme všechny 3 hodnoty najednou
                actions.onNameChanged(predefinedVaccine.vaccineName)
                actions.onDiseaseChanged(predefinedVaccine.disease)
                actions.onCategoryChanged(predefinedVaccine.category)
                showVaccinePickerDialog = false
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onBackground),
                title = { Text(if (isEditMode) "Upravit očkování" else "Přidat očkování") },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.returBack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, "Zpět", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { actions.deleteInjection() }) {
                            Icon(Icons.Outlined.Delete, "Odstranit", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { actions.saveInjection() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = data.nameError == null && data.diseaseError == null
                ) {
                    Text("Uložit", color = MyBlack)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(WindowInsets.ime.asPaddingValues())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Box(modifier = Modifier.clickable { showVaccinePickerDialog = true }) {
                    OutlinedTextField(
                        value = data.injection.name,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Název vakcíny") },
                        placeholder = { Text("Vybrat ze seznamu...") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Vybrat ze seznamu") },
                        isError = data.nameError != null,
                        singleLine = true,
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
                if (data.nameError != null) {
                    Text(
                        text = stringResource(id = data.nameError),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = data.injection.disease,
                    onValueChange = { actions.onDiseaseChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Chrání proti") },
                    isError = data.diseaseError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                if (data.diseaseError != null) {
                    Text(
                        text = stringResource(id = data.diseaseError),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            // OPRAVA: Pole Kategorie je nyní plně needitovatelné
            item {
                OutlinedTextField(
                    value = data.injection.category.displayName,
                    onValueChange = {},
                    label = { Text("Kategorie") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false, // Plně vypnuté
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onBackground,
                        disabledBorderColor = MaterialTheme.colorScheme.onBackground,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onBackground,
                        disabledLabelColor = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }

            // Datum
            item {
                OutlinedTextField(
                    value = DateUtils.getDateTimeString(data.injection.date),
                    onValueChange = { },
                    label = { Text("Datum očkování") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
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

            // Poznámka
            item {
                OutlinedTextField(
                    value = data.injection.note ?: "",
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
    }

    if (showDatePicker) {
        CustomDatePickerDialog(
            datePickerState = datePickerState,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                showDatePicker = false
                val cal = Calendar.getInstance()
                cal.timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                cal.set(Calendar.HOUR_OF_DAY, 12)
                cal.set(Calendar.MINUTE, 0)
                actions.onDateChanged(cal.timeInMillis)
            }
        )
    }
}

/**
 * Nová Composable funkce pro dialog s výběrem vakcíny.
 */
@Composable
private fun VaccinePickerDialog(
    onDismiss: () -> Unit,
    onVaccineSelected: (PredefinedVaccine) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column {
                Text(
                    text = "Vybrat očkování",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    // OPRAVA: Procházíme novou mapu
                    PredefinedVaccines.allVaccines.forEach { (sectionTitle, vaccines) ->
                        item {
                            VaccineSection(
                                title = sectionTitle,
                                vaccines = vaccines,
                                onVaccineSelected = onVaccineSelected
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("ZAVŘÍT")
                }
            }
        }
    }
}

/**
 * Pomocná komponenta pro zobrazení jedné sekce vakcín v dialogu.
 */
@Composable
private fun VaccineSection(
    title: String,
    vaccines: List<PredefinedVaccine>,
    onVaccineSelected: (PredefinedVaccine) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
        vaccines.forEach { vaccine ->
            ListItem(
                headlineContent = { Text(vaccine.vaccineName) },
                supportingContent = { Text(vaccine.disease) },
                modifier = Modifier
                    .clickable { onVaccineSelected(vaccine) }
                    .fillMaxWidth()
            )
            Divider(modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}