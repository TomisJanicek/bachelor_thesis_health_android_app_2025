package cz.tomasjanicek.bp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

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

            val appTheme by settingsManager.themeFlow.collectAsState()

            // 2. Vypočítáme, jestli má být tma
            val isDarkTheme = when (appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            // 1. Získáme si ovladač pro systémové UI (lišty)
            val systemUiController = rememberSystemUiController()

            // 2. Zjistíme, jestli je systém ve světlém nebo tmavém režimu.
            //    Pokud je ve světlém, potřebujeme tmavé ikony v lištách (a naopak).
            val useDarkIcons = !isSystemInDarkTheme()

            // 3. LaunchedEffect zajistí, že se toto nastavení provede pouze jednou
            //    a správně se přizpůsobí při změně světlého/tmavého režimu.

            LaunchedEffect(systemUiController, useDarkIcons) {
                // Nastavíme barvu horní lišty (status bar) na průhlednou
                systemUiController.setStatusBarColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons,
                )

                // Nastavíme barvu dolní lišty (navigation bar) na průhlednou
                systemUiController.setNavigationBarColor(
                    color = Color.Transparent, // <-- ZMĚNA ZPĚT: Barva je průhledná
                    darkIcons = useDarkIcons,
                    navigationBarContrastEnforced = false


                )
            }
            BpTheme(
                darkTheme = isDarkTheme
            ) {
                NavGraph(
                    startDestination = Destination.SplashScreen.route
                )
            }
        }
    }
}