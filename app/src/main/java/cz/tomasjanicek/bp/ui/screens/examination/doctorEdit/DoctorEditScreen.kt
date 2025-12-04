package cz.tomasjanicek.bp.ui.screens.examination.doctorEdit

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyWhite
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorEditScreen( // Přejmenováno
    navigationRouter: INavigationRouter,
    doctorId: Long, // Už není volitelný, protože vždy upravujeme
    latitudeFromResult: Double?,
    longitudeFromResult: Double?,
    onResultConsumed: () -> Unit
) {
    val viewModel: DoctorEditViewModel = hiltViewModel() // Přejmenováno
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Efekt, který se spustí POUZE JEDNOU, když dorazí výsledek z mapy
    LaunchedEffect(latitudeFromResult, longitudeFromResult) {
        Log.d("LocationFlow", "[DoctorEditScreen] LaunchedEffect se spustil s hodnotami: lat=${latitudeFromResult}, lng=${longitudeFromResult}")
        if (latitudeFromResult != null && longitudeFromResult != null) {
            Log.d("LocationFlow", "[DoctorEditScreen] Volám viewModel.handleLocationResult()")

            // Zavoláme metodu a počkáme, až její Job doběhne
            viewModel.handleLocationResult(latitudeFromResult, longitudeFromResult).join()

            // Až POTÉ, co ViewModel vše zpracuje, smažeme výsledek.
            Log.d("LocationFlow", "[DoctorEditScreen] ViewModel dokončil zpracování, volám onResultConsumed()")
            onResultConsumed()
        }
    }


    LaunchedEffect(key1 = doctorId) {
        viewModel.subscribeToDoctorUpdates(doctorId)
    }
    LaunchedEffect(uiState) {
        if (uiState is DoctorEditUIState.DoctorSaved) {
            navigationRouter.returBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upravit lékaře") },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.returBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                }
                // Odstraněna sekce `actions` pro mazání
            )
        },
        bottomBar = {
            // Data potřebujeme získat z aktuálního stavu
            val data = (uiState as? DoctorEditUIState.Success)?.data

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MyWhite)
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { viewModel.saveDoctor() },
                    modifier = Modifier.fillMaxWidth(),
                    // Tlačítko je aktivní, jen pokud je jméno validní a data jsou načtena
                    enabled = data != null && data.nameError == null
                ) {
                    Text("Uložit")
                }
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is DoctorEditUIState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DoctorEditUIState.Success -> {
                DoctorEditContent(
                    data = state.data,
                    actions = viewModel,
                    modifier = Modifier.padding(innerPadding),
                    // ZMĚNA: Předáváme logiku pro navigaci na mapu přímo sem
                    onSelectLocationOnMapClicked = {
                        val doctor = state.data.doctor
                        navigationRouter.navigateToMapSelectorScreen(
                            initialLatitude = doctor?.latitude,
                            initialLongitude = doctor?.longitude
                        )
                    }
                )
            }

            is DoctorEditUIState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(state.message)
                }
            }

            is DoctorEditUIState.DoctorSaved -> {
                // Tento stav už jen čeká na navigaci, není třeba nic zobrazovat
            }
        }
    }
}


@Composable
private fun DoctorEditContent( // Přejmenováno
    data: DoctorEditData,
    actions: DoctorEditAction,
    modifier: Modifier = Modifier,
    // ZMĚNA: Přidáváme nový parametr pro obsluhu kliknutí
    onSelectLocationOnMapClicked: () -> Unit
) {
    // Pokud se data ještě nenačetla, nezobrazuj nic
    val doctor = data.doctor ?: return
    val decimalFormat = DecimalFormat("#.######")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = doctor.specialization,
                style = MaterialTheme.typography.titleLarge,
                color = MyBlack
            )
        }
        // Jméno (povinné)
        item {
            OutlinedTextField(
                value = doctor.name.toString(),
                onValueChange = { actions.onNameChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Jméno a příjmení") },
                isError = data.nameError != null,
                singleLine = true,
                supportingText = {
                    if (data.nameError != null) {
                        Text(
                            stringResource(id = data.nameError),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
        // Telefon
        item {
            OutlinedTextField(
                value = doctor.phone ?: "",
                onValueChange = { actions.onPhoneChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Telefon") },
                singleLine = true
            )
        }
        // Email
        item {
            OutlinedTextField(
                value = doctor.email ?: "",
                onValueChange = { actions.onEmailChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true
            )
        }
        // Adresa s tlačítkem pro výběr
        item {
            OutlinedTextField(
                // --- ZMĚNA ZDE ---
                value = doctor.addressLabel ?: "", // Používáme přejmenované pole
                onValueChange = { actions.onLocationChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Adresa / Popis místa") }, // Můžeme vylepšit popisek
                trailingIcon = {
                    IconButton(onClick = onSelectLocationOnMapClicked) {
                        Icon(Icons.Default.Map, contentDescription = "Vybrat na mapě")
                    }
                }
            )
        }

        // Zobrazení souřadnic
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = doctor.latitude?.let { decimalFormat.format(it) } ?: "N/A",
                    onValueChange = { /* Read-only */ },
                    modifier = Modifier.weight(1f),
                    label = { Text("Latitude") },
                    readOnly = true,
                    enabled = false // Vizuálně odliší pole jako neaktivní
                )
                OutlinedTextField(
                    value = doctor.longitude?.let { decimalFormat.format(it) } ?: "N/A",
                    onValueChange = { /* Read-only */ },
                    modifier = Modifier.weight(1f),
                    label = { Text("Longitude") },
                    readOnly = true,
                    enabled = false // Vizuálně odliší pole jako neaktivní
                )
            }
        }

        // Popisek/Subtitle
        item {
            OutlinedTextField(
                value = doctor.subtitle ?: "",
                onValueChange = { actions.onSubtitleChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Popisek (např. ordinační hodiny)") },
                minLines = 3
            )
        }
    }
}