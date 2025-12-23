package cz.tomasjanicek.bp.ui.screens.measurement.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.bottomBar.CustomBottomBar
import cz.tomasjanicek.bp.ui.elements.EmptyStateScreen
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfMeasurementCategory(
    navigationRouter: INavigationRouter,
    viewModel: ListOfMeasurementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()


    Scaffold(
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
                title = {
                    Text(
                        "Měření",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.navigateToUserScreen() }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profil uživatele",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // --- NOVÉ TLAČÍTKO PRO RYCHLÉ PŘIDÁNÍ ---
                    IconButton(onClick = {
                        navigationRouter.navigateToDefaultCategories() // <-- Volání navigace
                    }) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Přidat doporučené",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    // ----------------------------------------

                    IconButton(onClick = { navigationRouter.navigateToSettingsScreen() }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Nastavení",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            CustomBottomBar(
                navigationRouter = navigationRouter,
                currentScreenIndex = 1 // 0 = Prohlídky, 1 = Měření
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigationRouter.navigateToAddEditMeasurementCategory(null)
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MyBlack
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add"
                )
            }
        },
    ) { innerPadding ->
        when (state) {
            ListOfMeasurementUIState.Loading -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            is ListOfMeasurementUIState.Content -> {
                val content = state as ListOfMeasurementUIState.Content

                // --- ZDE JE ZMĚNA ---
                if (content.categories.isEmpty()) {
                    // 1. Pokud je seznam prázdný -> Empty State
                    EmptyStateScreen(
                        modifier = Modifier.padding(innerPadding),
                        title = "Zatím žádná měření",
                        description = "Vytvořte si vlastní kategorie (např. Tlak, Váha, Cukr) a sledujte své zdraví v čase.",
                        buttonText = "Vytvořit kategorii", // Tlačítko uprostřed jako výzva k akci
                        onButtonClick = {
                            navigationRouter.navigateToAddEditMeasurementCategory(null)
                        }
                    )
                } else {
                    // 2. Pokud data jsou -> LazyColumn
                    LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(content.categories) { item ->
                            MeasurementCategoryItem(
                                categoryWithFields = item,
                                onClick = {
                                    navigationRouter.navigateToMeasurementCategoryDetail(item.category.id)
                                }
                            )
                        }
                        // Spacer, aby obsah nebyl schovaný pod FABem
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }

            ListOfMeasurementUIState.Error -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nastala chyba při načítání měření.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun MeasurementCategoryItem(
    categoryWithFields: MeasurementCategoryWithFields,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MyGreen.copy(alpha = 0.8f),
            contentColor = MyBlack
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = categoryWithFields.category.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (!categoryWithFields.category.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = categoryWithFields.category.description.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (categoryWithFields.fields.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Parametrů: ${categoryWithFields.fields.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MyBlack
                )
            }
        }
    }
}