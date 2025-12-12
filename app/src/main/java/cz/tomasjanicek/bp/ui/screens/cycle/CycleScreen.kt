package cz.tomasjanicek.bp.ui.screens.cycle

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.CustomBottomBar
import cz.tomasjanicek.bp.ui.screens.cycle.components.CalendarView
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyWhite
import cz.tomasjanicek.bp.ui.theme.TagPurple
import kotlinx.coroutines.delay
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleScreen(
    navigationRouter: INavigationRouter,
    currentScreenIndex: Int,
    viewModel: CycleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState.showEditDialog?.let { dialogInfo ->
        EditDayDialog(
            dialogInfo = dialogInfo,
            onDismiss = { viewModel.onAction(CycleAction.DismissEditDialog) },
            onLogMenstruation = { viewModel.onAction(CycleAction.LogMenstruation(dialogInfo.date)) },
            onLogOvulation = { viewModel.onAction(CycleAction.LogOvulation(dialogInfo.date)) },
            onDelete = { viewModel.onAction(CycleAction.DeleteEvent(dialogInfo.date)) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = { Text("Sledování cyklu") },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.navigateToUserScreen() }) {
                        Icon(Icons.Default.Person, "Profil", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Settings, "Nastavení", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        },
        bottomBar = {
            CustomBottomBar(
                navigationRouter = navigationRouter,
                currentScreenIndex = currentScreenIndex
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            MonthSelector(
                month = uiState.selectedMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                    .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                year = uiState.selectedMonth.year.toString(),
                onPrevious = { viewModel.onAction(CycleAction.PreviousMonthClicked) },
                onNext = { viewModel.onAction(CycleAction.NextMonthClicked) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            StatusCarouselAndStats(uiState)

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                CalendarView(
                    days = uiState.calendarDays,
                    onDayClick = { day ->
                        if (day > 0) {
                            val date = uiState.selectedMonth.atDay(day)
                            viewModel.onAction(CycleAction.ShowEditDialog(date))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            CycleLegend()
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StatusCarouselAndStats(uiState: CycleUIState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (uiState.statusCarouselItems.isNotEmpty()) {
            val pageCount = uiState.statusCarouselItems.size
            // Klíčujeme pagerState podle počtu stránek, aby se správně resetoval při změně
            val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })

            // --- ZDE JE VYLEPŠENÁ LOGIKA PRO PLYNULÉ PŘECHODY ---
            LaunchedEffect(pagerState, pageCount) {
                // Sledujeme, zda uživatel právě ručně neposouvá
                snapshotFlow { pagerState.isScrollInProgress }.collect { isScrolling ->
                    // Pokud uživatel neposouvá, je více než jedna stránka a automatické přehrávání je zapnuté
                    if (!isScrolling && pageCount > 1) {
                        // Spustíme nekonečnou smyčku pro automatické posouvání
                        while (true) {
                            delay(5000) // Počkáme 5 sekund
                            // Vypočítáme další stránku s bezpečným přetečením
                            val nextPage = (pagerState.currentPage + 1) % pageCount
                            // Spustíme plynulou animaci na další stránku
                            pagerState.animateScrollToPage(nextPage)
                        }
                    }
                }
            }
            // ---------------------------------------------------

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
                // Odebrali jsme .pointerInput, protože nám `isScrollInProgress` stačí
            ) { page ->
                Text(
                    text = uiState.statusCarouselItems[page],
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Průměr cyklu", uiState.averageCycleLength)
            StatItem("Průměr menstr.", uiState.averageMenstruationLength)
        }
    }
}

@Composable
private fun StatItem(label: String, value: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
        val displayText = if (value > 0) "$value dní" else "Málo dat"
        Text(text = displayText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun MonthSelector(
    month: String, year: String, onPrevious: () -> Unit, onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, "Předchozí měsíc", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(text = "$month $year", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, "Další měsíc", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun CycleLegend() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Row {
            Column(modifier = Modifier.weight(1f)) {
                LegendItem(color = MyPink, text = "Menstruace")
                LegendItem(color = MyGreen, text = "Plodné dny")
                LegendItem(color = TagPurple, text = "Ovulace")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                LegendItem(color = MyPink.copy(0.5f), text = "Očekávaná m.")
                LegendItem(color = MyGreen.copy(alpha = 0.5f), text = "Očekávané p. dny")
                LegendItem(color = TagPurple.copy(alpha = 0.5f), text = "Očekávaná o.")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
        Box(modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun EditDayDialog(
    dialogInfo: EditDialogInfo,
    onDismiss: () -> Unit,
    onLogMenstruation: () -> Unit,
    onLogOvulation: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upravit den: ${dialogInfo.date.dayOfMonth}. ${dialogInfo.date.monthValue}.") },
        text = { Text("Vyberte akci, kterou chcete pro tento den provést.") },
        confirmButton = {
            Column {
                Button(onClick = onLogMenstruation, modifier = Modifier.fillMaxWidth()) { Text("Zaznamenat menstruaci") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onLogOvulation, modifier = Modifier.fillMaxWidth()) { Text("Zaznamenat ovulaci") }

                if (dialogInfo.hasExistingRecord) {
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onDelete) {
                        Text("Smazat záznam z tohoto dne", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Zrušit") }
        }
    )
}