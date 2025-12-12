package cz.tomasjanicek.bp.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import cz.tomasjanicek.bp.auth.AuthRepository
import cz.tomasjanicek.bp.navigation.INavigationRouter
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navigationRouter: INavigationRouter,
    viewModel: SplashViewModel = hiltViewModel() // Hilt injektuje ViewModel
) {
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.Home -> navigationRouter.navigateToHomeFromLogin()
            SplashDestination.Login -> navigationRouter.navigateToLogin()
            SplashDestination.None -> { /* Čekáme */ }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Zde tvoje Logo nebo Loading
        CircularProgressIndicator()
    }
}