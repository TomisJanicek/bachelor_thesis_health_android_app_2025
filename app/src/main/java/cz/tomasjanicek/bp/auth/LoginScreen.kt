package cz.tomasjanicek.bp.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.loginState.collectAsState()
    val context = LocalContext.current // Toto je kontext Activity v Compose

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp), // Přidal jsem padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Google Button
        Button(
            onClick = { viewModel.onSignInClick(context) },
            enabled = state !is LoginState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Přihlásit se přes Google")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Oddělovač (volitelné, pro hezčí vzhled)
        Text("nebo", color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // 3. NOVÉ TLAČÍTKO - Lokální účet
        OutlinedButton(
            onClick = {
                viewModel.onContinueAsGuestClick {
                    onLoginSuccess()
                }
            },
            enabled = state !is LoginState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pokračovat bez přihlášení (pouze lokálně)")
        }

        // Reakce na stavy
        when (state) {
            is LoginState.Loading -> CircularProgressIndicator()
            is LoginState.Success -> {
                LaunchedEffect(Unit) {
                    onLoginSuccess() // Navigace pryč
                    viewModel.resetState() // <--- TOTO JE DŮLEŽITÉ: Vyčistíme stav pro příště
                }
            }
            is LoginState.Error -> {
                val msg = (state as LoginState.Error).message
                Text("Chyba: $msg", color = Color.Red)
                // Volitelně: Tlačítko "OK", které zavolá viewModel.resetState()
            }
            else -> {}
        }
    }
}