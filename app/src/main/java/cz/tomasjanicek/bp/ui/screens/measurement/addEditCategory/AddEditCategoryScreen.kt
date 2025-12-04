package cz.tomasjanicek.bp.ui.screens.measurement.addEditCategory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.copy
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.screens.measurement.addEditCategory.elements.ParameterEditDialog
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyWhite
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryScreen(
    navigationRouter: INavigationRouter,
    id: Long?
) {
    val viewModel = hiltViewModel<AddEditCategoryViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCategory(id)
    }

    LaunchedEffect(state) {
        when (state) {
            AddEditCategoryUIState.CategorySaved,
            AddEditCategoryUIState.CategoryDeleted -> {
                navigationRouter.returBack()
            }
            else -> {}
        }
    }

    val data = (state as? AddEditCategoryUIState.CategoryChanged)?.data

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MyWhite,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyWhite, titleContentColor = MyBlack),
                title = { Text(if (id == null) "Nová kategorie" else "Upravit kategorii") },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.returBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Zpět", tint = MyBlack)
                    }
                },
                actions = {
                    if (data?.category?.id != 0L) {
                        IconButton(onClick = { viewModel.deleteCategory() }) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Odstranit kategorii")
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
                    onClick = { viewModel.saveCategory() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = data != null
                ) {
                    Text("Uložit kategorii")
                }
            }
        }
    ) { innerPadding ->
        when (val currentState = state) {
            is AddEditCategoryUIState.CategoryChanged -> {AddEditCategoryContent(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(WindowInsets.ime.asPaddingValues()),
                data = currentState.data,
                actions = viewModel // <-- viewModel PŘEDÁVÁME DO AddEditCategoryContent
            )

                // Zobrazení dialogu
                if (currentState.data.isEditingParameter && currentState.data.editingField != null) {
                    ParameterEditDialog(
                        field = currentState.data.editingField,
                        error = currentState.data.editingFieldError,
                        onDismiss = { viewModel.onParameterDialogDismissed() },
                        onConfirm = { viewModel.onParameterSaved() },
                        // --- UPRAVENÉ VOLÁNÍ ---
                        onFieldChange = { label, unit, min, max ->
                            viewModel.onParameterFieldChanged(label, unit, min, max)
                        }
                    )
                }
            }
            is AddEditCategoryUIState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun AddEditCategoryContent(
    modifier: Modifier = Modifier,
    data: AddEditCategoryData,
    actions: AddEditCategoryAction
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SEKCIE 1: ZÁKLADNÍ INFORMACE ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Základní informace", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = data.category.name,
                onValueChange = { actions.onNameChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Název kategorie*") },
                isError = data.nameError != null,
                singleLine = true
            )
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
                value = data.category.description.orEmpty(),
                onValueChange = { actions.onDescriptionChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Popis (nepovinné)") },
                minLines = 2
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- SEKCIE 2: PARAMETRY PRO MĚŘENÍ ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Parametry", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = { actions.onParameterDialogOpened(null) }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Přidat")
                }
            }
            if (data.fieldsError != null) {
                Text(
                    text = stringResource(id = data.fieldsError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (data.fields.isEmpty()) {
                Text(
                    text = "Kategorie musí mít alespoň jeden parametr.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // SEZNAM PARAMETRŮ
        items(data.fields, key = { it.id }) { field ->
            ParameterListItem(
                field = field,
                onEditClick = { actions.onParameterDialogOpened(field) },
                onDeleteClick = { actions.onParameterDeleted(field) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ParameterListItem(
    field: MeasurementCategoryField,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded } // Přepíná rozbalení po kliknutí
            .animateContentSize(animationSpec = spring()), // <-- Plynulá animace rozbalení
        colors = CardDefaults.cardColors(containerColor = MyGreen.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- VŽDY VIDITELNÁ ČÁST ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f) // Zajistí, že text zabere dostupné místo
                )
                // Jemná šipka, která indikuje stav rozbalení
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Rozbalit/Sbalit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- ROZBALOVACÍ ČÁST (zobrazí se po kliknutí) ---
            if (isExpanded) {
                // Detailní informace (klíč a jednotka)
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (field.unit != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Jednotka: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(field.unit, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (field.minValue != null || field.maxValue != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rozsah: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            val rangeText = buildString {
                                if (field.minValue != null) append(field.minValue.toString())
                                else append("...")
                                append(" - ")
                                if (field.maxValue != null) append(field.maxValue.toString())
                                else append("...")
                                if(field.unit != null) append(" ${field.unit}")
                            }
                            Text(rangeText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Oddělovač a akční tlačítka
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End, // Tlačítka zarovnáme na konec
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tlačítka jsou teď decentní TextButton
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