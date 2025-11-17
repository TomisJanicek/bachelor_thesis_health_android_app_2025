package cz.tomasjanicek.bp.ui.screens.examination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.CustomBottomBar
import cz.tomasjanicek.bp.ui.elements.CustomExaminationRow
import cz.tomasjanicek.bp.ui.theme.MyBlack
import java.time.LocalDateTime
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.text.style.TextDecoration
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationStatus // Importuj tvůj enum
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import cz.tomasjanicek.bp.ui.theme.MyWhite
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfExaminationScreen(
    navigationRouter: INavigationRouter,
    currentScreenIndex: Int
){

    // 1. ZÍSKÁNÍ DAT Z VIEWMODELU (toto už máš správně)
    val viewModel = hiltViewModel<ListOfExaminationViewModel>()
    val uiState by viewModel.listOfExaminationUIState.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var selectedFilter by remember { mutableStateOf(ExaminationFilterType.SCHEDULED) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MyWhite,
        topBar = {
            MediumTopAppBar(
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
                    Text(
                        "Prohlídky",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Navigace na profil */ }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profil uživatele"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Navigace do nastavení */ }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Nastavení"
                        )
                    }
                },
                // 3. Předání rolovacího chování do TopAppBar
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
                onClick = {
                    //TODO(navigationRouter.navigateToAddEdit(null))
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MyBlack
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

        ) {
            StatusSelector(
                selectedFilter = selectedFilter,
                onFilterSelected = { newFilter -> selectedFilter = newFilter },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 2. ZPRACOVÁNÍ STAVŮ Z VIEWMODELU
            when (val currentState = uiState) {
                is ListOfExaminationUIState.Loading -> {
                    // Stav načítání - zobrazíme kolečko uprostřed
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ListOfExaminationUIState.Success -> {
                    // Stav úspěchu - máme data, tak je zobrazíme
                    ListOfExaminationScreenContent(
                        examinations = currentState.examinationList,
                        selectedFilter = selectedFilter,
                        navigationRouter = navigationRouter
                    )
                }
            }
        }
    }
}

@Composable
fun ListOfExaminationScreenContent(
    examinations: List<ExaminationWithDoctor>,
    selectedFilter: ExaminationFilterType, // Změna zde
    navigationRouter: INavigationRouter
){
    // 3. FILTROVÁNÍ DAT PODLE VYBRANÉHO STAVU
    val filteredExaminations = remember(examinations, selectedFilter) {
        val allSorted = examinations.sortedByDescending { it.examination.dateTime }
        when (selectedFilter) {
            ExaminationFilterType.SCHEDULED ->
                allSorted.filter { it.examination.status == ExaminationStatus.PLANNED }
            ExaminationFilterType.HISTORY ->
                allSorted.filter {
                    it.examination.status == ExaminationStatus.COMPLETED ||
                            it.examination.status == ExaminationStatus.CANCELLED
                }
        }
    }

    // 4. ZOBRAZENÍ SEZNAMU NEBO ZPRÁVY "ŽÁDNÉ POLOŽKY"
    if (filteredExaminations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text = "Pro tento filtr nebyly nalezeny žádné prohlídky.")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredExaminations, key = { it.examination.id }) { examination ->
                CustomExaminationRow(
                    item = examination,
                    onClick = {
                        // navigationRouter.navigateToExaminationDetail(item.examination.id)
                    }
                )
            }
        }
    }
}



// Enum pro stavy přepínače

enum class ExaminationFilterType(val label: String) {
    SCHEDULED("Naplánované"),
    HISTORY("Historie")
}

@Composable
fun StatusSelector(
    selectedFilter: ExaminationFilterType, // Změna
    onFilterSelected: (ExaminationFilterType) -> Unit, // Změna
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    SingleChoiceSegmentedButtonRow(modifier) {
        // Použijeme náš nový enum pro UI
        ExaminationFilterType.values().forEachIndexed { index, filterType ->
            SegmentedButton(
                selected = selectedFilter == filterType,
                onClick = { onFilterSelected(filterType) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ExaminationFilterType.values().size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = colors.secondary,
                    activeContentColor = colors.onPrimary,
                    activeBorderColor = MyBlack,

                    inactiveContainerColor = MyWhite,
                    inactiveContentColor = MyBlack,
                    inactiveBorderColor = MyBlack
                )
            ) {
                Text(filterType.label)
            }
        }
    }
}