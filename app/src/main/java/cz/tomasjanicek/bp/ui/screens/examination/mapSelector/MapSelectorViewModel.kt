package cz.tomasjanicek.bp.ui.screens.examination.mapSelector

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.copy
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import android.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.concurrent.Executors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MapSelectorViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapSelectorState())
    val uiState = _uiState.asStateFlow()

    // Vytvoříme si klienta pro získávání polohy
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun loadInitialPosition(initialLatitude: Double?, initialLongitude: Double?) {
        if (initialLatitude != null && initialLongitude != null) {
            val initialPos = LatLng(initialLatitude, initialLongitude)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedPosition = initialPos,
                    initialCameraPosition = initialPos
                )
            }
        } else {
            // Jinak se pokusíme získat aktuální polohu zařízení.
            fetchCurrentDeviceLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentDeviceLocation() {
        _uiState.update { it.copy(isLoading = true) }

        // --- ZCELA NOVÁ IMPLEMENTACE ---

        // 1. Zkusíme okamžitě získat poslední známou polohu
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Skvělé, máme polohu okamžitě. Aktualizujeme UI.
                updateUiWithLocation(location)
            }
            // Pokud je poloha null nebo chceme ještě přesnější, pokračujeme dál.
        }

        // 2. Požádáme o vysoce přesnou AKTUÁLNÍ polohu.
        //    Toto může chvíli trvat, ale obvykle je to mnohem rychlejší než LocationManager.
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Máme čerstvou a přesnou polohu.
                updateUiWithLocation(location)
            }
        }.addOnFailureListener {
            // Zde bychom mohli řešit chyby (např. GPS je vypnuté)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // Pomocná funkce, abychom neopakovali kód
    private fun updateUiWithLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        _uiState.update {
            it.copy(
                isLoading = false,
                userPosition = latLng,
                // Pokud ještě není nic vybráno, zaměříme kameru na uživatele
                initialCameraPosition = it.initialCameraPosition ?: latLng
            )
        }
    }

    fun onMapClick(position: LatLng) {
        _uiState.update { it.copy(selectedPosition = position) }
    }
}