package cz.tomasjanicek.bp.ui.screens.medicine.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.CustomBottomBar
import cz.tomasjanicek.bp.ui.screens.medicine.components.MedicineReminderItem
import cz.tomasjanicek.bp.ui.theme.MyBlack
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.text.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(
    navigationRouter: INavigationRouter,
    currentScreenIndex: Int,
    viewModel: MedicineListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale("cs", "CZ")) }

    // Formátujeme datum z ViewModelu, ne jen dnešek
    val formattedDate = remember(state.selectedDate) {
        val date = state.selectedDate
        val today = LocalDate.now()
        when {
            date == today -> "Dnes, ${date.format(dateFormatter).substringAfter(", ")}"
            date == today.plusDays(1) -> "Zítra, ${date.format(dateFormatter).substringAfter(", ")}"
            date == today.minusDays(1) -> "Včera, ${date.format(dateFormatter).substringAfter(", ")}"
            else -> date.format(dateFormatter).replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
    }


    // Stavy pro modální okno
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedReminder by remember { mutableStateOf<MedicineReminder?>(null) }

    // --- DIALOG S DETAILEM LÉKU ---
    if (showDetailDialog && selectedReminder != null) {
        val reminder = selectedReminder!!
        val medicine = state.medicineDetails[reminder.medicineId]

        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = { Text(medicine?.name ?: "Detail léku") },
            text = {
                if (medicine != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Dávka: ${medicine.dosage.toInt()} ${medicine.unit.label}")
                        if (!medicine.note.isNullOrBlank()) {
                            Text("Poznámka: ${medicine.note}")
                        }
                    }
                } else {
                    Text("Načítání detailů...")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showDetailDialog = false
                    // Navigujeme na úpravu s ID celého nastavení léku
                    navigationRouter.navigateToAddEditMedicine(reminder.medicineId)
                }) {
                    Text("Upravit")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.onAction(MedicineListAction.OnDeleteMedicineClicked(reminder.medicineId))
                            showDetailDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Smazat")
                    }
                    TextButton(onClick = { showDetailDialog = false }) {
                        Text("Zavřít")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Moje léky") }
            )
        },
        bottomBar = {
            CustomBottomBar(
                navigationRouter = navigationRouter,
                currentScreenIndex = currentScreenIndex
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigationRouter.navigateToAddEditMedicine() },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MyBlack
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Přidat lék")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Nový prvek pro výběr dne ---
            item {
                DateSelector(
                    dateText = formattedDate,
                    onPreviousDay = {
                        viewModel.onAction(MedicineListAction.OnDateChanged(state.selectedDate.minusDays(1)))
                    },
                    onNextDay = {
                        viewModel.onAction(MedicineListAction.OnDateChanged(state.selectedDate.plusDays(1)))
                    },
                    modifier = Modifier.padding(horizontal = 4.dp) // Snížení paddingu kvůli IconButton
                )
                Spacer(Modifier.height(8.dp))
            }

            // --- Sekce: Naplánované ---
            if (state.todaysPlanned.isNotEmpty()) {
                item {
                    ListSectionHeader("Naplánované")
                }
                items(state.todaysPlanned, key = { "planned-${it.id}" }) { reminder ->
                    val medicine = state.medicineDetails[reminder.medicineId]
                    MedicineReminderItem(
                        reminder = reminder,
                        medicine = medicine,
                        onCheckedChange = { isChecked ->
                            viewModel.onAction(MedicineListAction.OnReminderToggled(reminder.id, isChecked))
                        },
                        onClick = {
                            selectedReminder = reminder
                            showDetailDialog = true
                        }
                    )
                }
            }

            // --- Sekce: Dokončené ---
            if (state.todaysCompleted.isNotEmpty()) {
                item {
                    ListSectionHeader("Dokončené")
                }
                items(state.todaysCompleted, key = { "completed-${it.id}" }) { reminder ->
                    val medicine = state.medicineDetails[reminder.medicineId]
                    MedicineReminderItem(
                        reminder = reminder,
                        medicine = medicine,
                        onCheckedChange = { isChecked ->
                            viewModel.onAction(MedicineListAction.OnReminderToggled(reminder.id, isChecked))
                        },
                        onClick = {
                            selectedReminder = reminder
                            showDetailDialog = true
                        }
                    )
                }
            }

            // --- Prázdný stav ---
            if (state.todaysPlanned.isEmpty() && state.todaysCompleted.isEmpty()) {
                item {
                    EmptyState(
                        modifier = Modifier.fillParentMaxSize(),
                        isToday = state.selectedDate == LocalDate.now() // Předáme info, zda je to dnes
                    )
                }
            }

            // Mezera na konci seznamu, aby FAB nepřekrýval poslední položku
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * Nový Composable pro zobrazení data a šipek pro přepínání.
 */
@Composable
private fun DateSelector(
    dateText: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Předchozí den", tint = MyBlack)
        }
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleLarge,
            color = MyBlack,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNextDay) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Následující den", tint = MyBlack)
        }
    }
}


@Composable
private fun ListSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        color = MyBlack
    )
}

// Upravený EmptyState, aby zobrazoval relevantní text
@Composable
private fun EmptyState(modifier: Modifier = Modifier, isToday: Boolean) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isToday) "Pro dnešek nemáte žádné léky." else "Pro tento den nemáte žádné léky.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            "Přidejte si nový lék pomocí tlačítka (+).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}