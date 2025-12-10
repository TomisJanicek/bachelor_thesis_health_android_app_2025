package cz.tomasjanicek.bp.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import cz.tomasjanicek.bp.auth.AuthRepository
import cz.tomasjanicek.bp.navigation.INavigationRouter
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navigationRouter: INavigationRouter,
    repository: AuthRepository // Injecneme přímo nebo přes ViewModel
) {
    LaunchedEffect(Unit) {
        // 1. Umělé zdržení (např. 2 sekundy), aby uživatel viděl logo
        delay(2000)

        val isLoggedInFirebase = repository.getCurrentUser() != null
        val isGuest = repository.isGuestMode()

        if (isLoggedInFirebase || isGuest) {
            // Pustíme ho dál, pokud je přihlášen NEBO je host
            navigationRouter.navigateToHomeFromLogin()
        } else {
            // Jinak Login
            navigationRouter.navigateToLogin()
        }
    }

    // UI Splash screenu
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Zde můžeš dát své logo (nahraď R.drawable.ic_launcher_foreground něčím svým)
        // Image(
        //     painter = painterResource(id = R.drawable.ic_launcher_foreground),
        //     contentDescription = "Logo",
        //     modifier = Modifier.size(150.dp)
        // )

        // Nebo zatím jen loading
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}