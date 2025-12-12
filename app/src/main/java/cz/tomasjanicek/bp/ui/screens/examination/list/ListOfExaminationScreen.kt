package cz.tomasjanicek.bp.ui.screens.examination.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.bottomBar.CustomBottomBar
import cz.tomasjanicek.bp.ui.elements.CustomExaminationRow
import cz.tomasjanicek.bp.ui.theme.MyBlack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.ui.Alignment
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import cz.tomasjanicek.bp.model.Injection
import cz.tomasjanicek.bp.ui.elements.CustomInjectionRow
import cz.tomasjanicek.bp.ui.elements.EmptyStateScreen
//import cz.tomasjanicek.bp.ui.elements.StatusSelector
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfExaminationScreen(
    navigationRouter: INavigationRouter,
    currentScreenIndex: Int
) {
    val viewModel = hiltViewModel<ListOfExaminationViewModel>()
    val uiState by viewModel.listOfExaminationUIState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    var selectedFilter by remember { mutableStateOf(ScreenFilterType.SCHEDULED) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MediumTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = { Text("Zdravotní záznamy", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.navigateToUserScreen() }) {
                        Icon(imageVector = Icons.Filled.Person, contentDescription = "Profil uživatele")
                    }
                },
                actions = {
                    IconButton(onClick = { navigationRouter.navigateToSettingsScreen() }) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Nastavení")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            CustomBottomBar(navigationRouter = navigationRouter, currentScreenIndex = currentScreenIndex)
        },
        floatingActionButton = {
            MultiFloatingActionButton(
                isExpanded = isFabMenuExpanded,
                onFabClick = { isFabMenuExpanded = !isFabMenuExpanded },
                onAddExaminationClick = {
                    isFabMenuExpanded = false
                    navigationRouter.navigateToAddEditExaminationScreen(null)
                },
                onAddInjectionClick = {
                    isFabMenuExpanded = false
                    navigationRouter.navigateToAddEditInjectionScreen(null)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FilterSelector(
                selectedFilter = selectedFilter,
                onFilterSelected = { newFilter -> selectedFilter = newFilter },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (val currentState = uiState) {
                is ListOfExaminationUIState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ListOfExaminationUIState.Success -> {
                    ListOfRecordsContent(
                        scheduledExaminations = currentState.scheduledExaminations,
                        historyExaminations = currentState.historyExaminations,
                        allInjections = currentState.allInjections,
                        selectedFilter = selectedFilter,
                        navigationRouter = navigationRouter
                    )
                }
            }
        }
    }
}

@Composable
fun ListOfRecordsContent(
    scheduledExaminations: List<ExaminationWithDoctor>,
    historyExaminations: List<ExaminationWithDoctor>,
    allInjections: List<Injection>,
    selectedFilter: ScreenFilterType,
    navigationRouter: INavigationRouter
) {
    when (selectedFilter) {
        ScreenFilterType.SCHEDULED, ScreenFilterType.HISTORY -> {
            val examinationsToShow = if (selectedFilter == ScreenFilterType.SCHEDULED) {
                scheduledExaminations
            } else {
                historyExaminations
            }

            if (examinationsToShow.isEmpty()) {
                EmptyStateScreen(
                    title = "Tady nic není",
                    description = "Klikněte na tlačítko níže a přidejte.",
                    buttonText = "Přidat vyšetření",
                    onButtonClick = { navigationRouter.navigateToAddEditExaminationScreen(null) })
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(examinationsToShow, key = { it.examination.id!! }) { examination ->
                        CustomExaminationRow(
                            item = examination,
                            onClick = {
                                val doctorId = examination.doctor?.id
                                if (doctorId != null) {
                                    navigationRouter.navigateToExaminationDetail(doctorId)
                                }
                            }
                        )
                    }
                }
            }
        }
        ScreenFilterType.INJECTIONS -> {
            if (allInjections.isEmpty()) {
                EmptyStateScreen(
                    title = "Žádné očkování",
                    description = "Nebyly nalezeny žádné záznamy o očkování. Klikněte na tlačítko níže a přidejte první.",
                    buttonText = "Přidat očkování",
                    onButtonClick = { navigationRouter.navigateToAddEditInjectionScreen(null)})
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(allInjections, key = { it.id }) { injection ->
                        CustomInjectionRow(
                            item = injection,
                            onClick = {
                                navigationRouter.navigateToAddEditInjectionScreen(injection.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSelector(
    selectedFilter: ScreenFilterType,
    onFilterSelected: (ScreenFilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        ScreenFilterType.values().forEach { filter ->
            SegmentedButton(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                shape = SegmentedButtonDefaults.itemShape(
                    baseShape = MaterialTheme.shapes.medium,
                    index = filter.ordinal,
                    count = ScreenFilterType.values().size
                ),
                // --- TATO ČÁST CHYBĚLA ---
                // Zde explicitně definujeme barvy přesně jako ve vašem StatusSelectoru
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MyPink, // Vaše barva pro aktivní tlačítko
                    activeContentColor = MyBlack,             // Vaše barva textu na aktivním
                    activeBorderColor = MaterialTheme.colorScheme.onBackground,              // Vaše barva rámečku aktivního

                    inactiveContainerColor = MaterialTheme.colorScheme.background,           // Vaše barva pro neaktivní
                    inactiveContentColor = MaterialTheme.colorScheme.onBackground,           // Vaše barva textu na neaktivním
                    inactiveBorderColor = MaterialTheme.colorScheme.onBackground             // Vaše barva rámečku neaktivního
                )
                // --- KONEC OPRAVY ---
            ) {
                Text(filter.label)
            }
        }
    }
}

@Composable
fun MultiFloatingActionButton(
    isExpanded: Boolean,
    onFabClick: () -> Unit,
    onAddExaminationClick: () -> Unit,
    onAddInjectionClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            // Použijeme Column pro seřazení malých FAB tlačítek
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tlačítko pro Očkování
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Textový popisek
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MyWhite)
                    ) {
                        Text(
                            text = "Evidovat očkování",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MyBlack
                        )
                    }
                    // Samotné tlačítko
                    SmallFloatingActionButton(
                        onClick = onAddInjectionClick,
                        containerColor = MyPink, // Vaše barva
                        contentColor = MyBlack    // Vaše barva
                    ) {
                        Icon(Icons.Default.Vaccines, "Evidovat očkování")
                    }
                }

                // Tlačítko pro Prohlídku
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Textový popisek
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MyWhite)
                    ) {
                        Text(
                            text = "Přidat prohlídku",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MyBlack
                        )
                    }
                    // Samotné tlačítko
                    SmallFloatingActionButton(
                        onClick = onAddExaminationClick,
                        containerColor = MyPink, // Vaše barva
                        contentColor = MyBlack    // Vaše barva
                    ) {
                        Icon(Icons.Default.Event, "Přidat prohlídku")
                    }
                }
            }
        }

        // Hlavní FAB tlačítko
        Spacer(Modifier.height(24.dp)) // Zvětšíme mezeru
        FloatingActionButton(
            onClick = onFabClick,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MyBlack
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (isExpanded) "Zavřít menu" else "Přidat záznam"
            )
        }
    }
}

enum class ScreenFilterType(val label: String) {
    SCHEDULED("Naplánované"),
    HISTORY("Historie"),
    INJECTIONS("Očkování")
}