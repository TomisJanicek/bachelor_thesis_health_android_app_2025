package cz.tomasjanicek.bp.ui.screens.medicine.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.text.style.TextOverflow
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.bottomBar.CustomBottomBar
import cz.tomasjanicek.bp.ui.elements.EmptyStateScreen
import cz.tomasjanicek.bp.ui.screens.medicine.components.MedicineReminderItem
import cz.tomasjanicek.bp.ui.theme.MyBlack
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MedicineListScreen(
    navigationRouter: INavigationRouter,
    currentScreenIndex: Int,
    viewModel: MedicineListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale("cs", "CZ")) }

    // Formátujeme datum z ViewModelu
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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = { Text("Moje léky", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.navigateToUserScreen() }) {
                        Icon(imageVector = Icons.Filled.Person, contentDescription = "Profil uživatele")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Navigace do nastavení */ }) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Nastavení")
                    }
                },
                scrollBehavior = scrollBehavior
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

        // --- HLAVNÍ PODMÍNKA PRO NAČÍTÁNÍ ---
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- Výběr dne ---
                item(key = "date-selector") {
                    DateSelector(
                        dateText = formattedDate,
                        onPreviousDay = {
                            viewModel.onAction(MedicineListAction.OnDateChanged(state.selectedDate.minusDays(1)))
                        },
                        onNextDay = {
                            viewModel.onAction(MedicineListAction.OnDateChanged(state.selectedDate.plusDays(1)))
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // --- Sekce: Naplánované ---
                if (state.todaysPlanned.isNotEmpty()) {
                    item(key = "header-planned") {
                        Box(modifier = Modifier.animateItem()) {
                            ListSectionHeader("Naplánované")
                        }
                    }
                    items(
                        items = state.todaysPlanned,
                        // DŮLEŽITÉ: Používáme stabilní klíč bez prefixu "planned", aby animace fungovala i při přesunu
                        key = { "reminder-${it.id}" }
                    ) { reminder ->
                        val medicine = state.medicineDetails[reminder.medicineId]
                        Box(modifier = Modifier.animateItem()) {
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
                }

                // --- Sekce: Dokončené ---
                if (state.todaysCompleted.isNotEmpty()) {
                    item(key = "header-completed") {
                        Box(modifier = Modifier.animateItem()) {
                            ListSectionHeader("Dokončené")
                        }
                    }
                    items(
                        items = state.todaysCompleted,
                        // DŮLEŽITÉ: Používáme stejný klíč jako nahoře, aby Compose věděl, že je to stejná položka
                        key = { "reminder-${it.id}" }
                    ) { reminder ->
                        val medicine = state.medicineDetails[reminder.medicineId]
                        Box(modifier = Modifier.animateItem()) {
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
                }

                // --- Prázdný stav s animací ---
                // Zobrazí se jen pokud NEJSME v loading stavu a seznamy jsou prázdné
                item(key = "empty-state") {
                    AnimatedVisibility(
                        visible = state.todaysPlanned.isEmpty() && state.todaysCompleted.isEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        EmptyStateScreen(
                            title = if (state.selectedDate == LocalDate.now()) "Pro dnešek nemáte žádné léky." else "Pro tento den nemáte žádné léky.",
                            description = "Přidejte si nový lék pomocí tlačítka (+)."
                        )
                    }
                }

                // Mezera na konci
                item(key = "bottom-spacer") {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Předchozí den", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNextDay) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Následující den", tint = MaterialTheme.colorScheme.onBackground)
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
        color = MaterialTheme.colorScheme.onBackground
    )
}