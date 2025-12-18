package cz.tomasjanicek.bp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

import com.google.accompanist.systemuicontroller.rememberSystemUiController
import cz.tomasjanicek.bp.auth.AuthRepository
import cz.tomasjanicek.bp.navigation.Destination
import cz.tomasjanicek.bp.navigation.NavGraph
import cz.tomasjanicek.bp.ui.screens.settings.SettingsManager
import cz.tomasjanicek.bp.ui.theme.AppTheme
import cz.tomasjanicek.bp.ui.theme.BpTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            // --- 1. Logika pro Téma (Tvůj původní kód) ---
            val appTheme by settingsManager.themeFlow.collectAsState()

            val isDarkTheme = when (appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            // --- 2. Logika pro System UI (Lišty) ---
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isDarkTheme // Upraveno: Ikony mají být tmavé, když JE světlé téma

            LaunchedEffect(systemUiController, useDarkIcons) {
                systemUiController.setStatusBarColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons,
                )
                systemUiController.setNavigationBarColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons,
                    navigationBarContrastEnforced = false
                )
            }

            BpTheme(
                darkTheme = isDarkTheme
            ) {
                // --- 3. NOVÉ: Žádost o notifikace (Android 13+) ---
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        // Zde můžeš reagovat na výsledek, např. logovat
                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permission = Manifest.permission.POST_NOTIFICATIONS
                        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(permission)
                        }
                    }
                }
                // --------------------------------------------------

                NavGraph(
                    startDestination = Destination.SplashScreen.route
                )
            }
        }
    }
}