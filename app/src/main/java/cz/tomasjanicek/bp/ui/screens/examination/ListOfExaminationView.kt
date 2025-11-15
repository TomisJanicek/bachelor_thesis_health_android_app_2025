package cz.tomasjanicek.bp.ui.screens.examination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.model.ExaminationType
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.CustomBottomBar
import cz.tomasjanicek.bp.ui.elements.CustomExaminationRow
import cz.tomasjanicek.bp.ui.theme.TopBarBackground
import cz.tomasjanicek.bp.ui.theme.TopBarContent
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfExaminationView(
    navigationRouter: INavigationRouter,
    currentScreenIndex: Int
){

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var selectedStatus by remember { mutableStateOf(ExaminationStatus.SCHEDULED) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    // Použij primární barvu z tvého tématu (která je MyGreen)
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.primary,

                    // Barva pro nadpis a ikony
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
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
        }
    ) { innerPadding ->
        Column( // Použijeme Column, abychom mohli umístit přepínač nad seznam
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- PŘIDANÝ PŘEPÍNAČ ---
            StatusSelector(
                selectedStatus = selectedStatus,
                onStatusSelected = { newStatus -> selectedStatus = newStatus },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            // --- KONEC PŘIDANÉHO PŘEPÍNAČE ---


            // Seznam položek
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Zde bude logika pro zobrazení položek podle `selectedStatus`
                when (selectedStatus) {
                    ExaminationStatus.SCHEDULED -> {
                        items(7) {
                            CustomExaminationRow(
                                title = "Praktik neuronů #$it",
                                subtitle = "Jdu preventivně a protože se fakt bojím tak nevím co s tím",
                                dateTime = LocalDateTime.of(2025, 12, 25, 22, 22),
                                type = cz.tomasjanicek.bp.ui.elements.ExaminationType.PROHLIDKA,
                                iconRes = R.drawable.ic_launcher_foreground // nahraď svým assetem
                            )
                        }
                    }
                    ExaminationStatus.HISTORY -> {
                        items(50) {
                            Text(
                                text = "Historická položka #$it",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}// Enum pro stavy přepínače
enum class ExaminationStatus(val label: String) {
    SCHEDULED("Naplánované"),
    HISTORY("Historie")
}

@Composable
fun StatusSelector(
    selectedStatus: ExaminationStatus,
    onStatusSelected: (ExaminationStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    SingleChoiceSegmentedButtonRow(modifier) {
        ExaminationStatus.values().forEachIndexed { index, status ->
            SegmentedButton(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ExaminationStatus.values().size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = colors.secondary,
                    activeContentColor = colors.onPrimary,
                    inactiveContainerColor = colors.surface,
                    inactiveContentColor = colors.onSurface
                )
            ) {
                Text(status.label)
            }
        }
    }
}