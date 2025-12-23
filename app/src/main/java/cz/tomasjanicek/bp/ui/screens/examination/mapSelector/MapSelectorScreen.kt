package cz.tomasjanicek.bp.ui.screens.examination.mapSelector

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import cz.tomasjanicek.bp.navigation.INavigationRouter
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class) // <-- Přidat tuto anotaci
@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("MissingPermission")
@Composable
fun MapSelectorScreen(
    navigationRouter: INavigationRouter,
    initialLatitude: Double? = null,
    initialLongitude: Double? = null
) {
    val viewModel: MapSelectorViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val cameraPositionState = rememberCameraPositionState()
    val scope = rememberCoroutineScope() // Získáme coroutine scope

    // --- ZMĚNA ZDE: Přidáme logiku pro oprávnění ---
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )


    LaunchedEffect(Unit) {
        // Při prvním spuštění zkusíme získat polohu, jen pokud už máme oprávnění
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.loadInitialPosition(initialLatitude, initialLongitude)
        } else {
            // Pokud nemáme, požádáme o ně
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vybrat polohu") },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.returBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // --- ZMĚNA ZDE: Logika pro kliknutí na FAB ---
                if (locationPermissionsState.allPermissionsGranted) {
                    // Pokud máme oprávnění, získáme polohu
                    viewModel.fetchCurrentDeviceLocation()
                } else {
                    // Pokud nemáme, požádáme o ně znovu
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            }) {
                Icon(Icons.Default.MyLocation, "Moje poloha")
            }
        },
        bottomBar = {
            val positionToSave = uiState.selectedPosition
            Button(
                onClick = {
                    if (positionToSave != null) {
                        Log.d("LocationFlow", "[MapSelectorScreen] Vracím výsledek: lat=${positionToSave.latitude}, lng=${positionToSave.longitude}")
                        navigationRouter.returnWithResult(
                            "latitude" to positionToSave.latitude,   // Toto je Double
                            "longitude" to positionToSave.longitude  // Toto je také Double
                        )
                    } else {
                        navigationRouter.returBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(16.dp),
                enabled = positionToSave != null // Tlačítko je aktivní, jen když je něco vybráno
            ) {
                Text("Uložit vybranou polohu")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false), // Schováme +/- tlačítka
                onMapClick = viewModel::onMapClick,
                onMapLoaded = {
                    // Tento kód se spustí, až když je mapa 100% připravená
                    uiState.initialCameraPosition?.let { position ->
                        // Použijeme scope, který jsme si vytvořili výše
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(position, 15f)
                                )
                            )
                        }
                    }
                }
            ) {
                // Značka pro aktuální polohu uživatele
                uiState.userPosition?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Vaše poloha"
                    )
                }
                // Značka pro vybranou polohu
                uiState.selectedPosition?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Vybraná poloha",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}