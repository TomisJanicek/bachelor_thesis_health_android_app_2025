package cz.tomasjanicek.bp.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cz.tomasjanicek.bp.R
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyWhite

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.loginState.collectAsState()
    val context = LocalContext.current

    // Reakce na úspěšné přihlášení
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = MyWhite
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // --- 1. LOGO A HLAVIČKA ---
                Icon(
                    imageVector = Icons.Default.HealthAndSafety, // Nebo tvoje logo R.drawable.logo
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MyGreen
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Zdravotní deník",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MyBlack
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mějte své zdraví pod kontrolou.\nJednoduše a přehledně.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(48.dp))

                // --- 2. CHYBOVÁ HLÁŠKA ---
                AnimatedVisibility(visible = state is LoginState.Error) {
                    val errorMsg = (state as? LoginState.Error)?.message
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Chyba",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMsg ?: "Neznámá chyba",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // --- 3. GOOGLE TLAČÍTKO ---
                // Google vyžaduje specifický branding, zkusíme se přiblížit
                Button(
                    onClick = { viewModel.onSignInClick(context) },
                    enabled = state !is LoginState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4), // Google Blue
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF4285F4).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(25.dp), // Zakulacené rohy
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    // Pokud máš SVG logo Google (R.drawable.ic_google), dej ho sem místo Spaceru
                    Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, tint = Color.Unspecified)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Přihlásit se přes Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 4. ODDĚLOVAČ ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.LightGray
                    )
                    Text(
                        text = "nebo",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 5. LOKÁLNÍ TLAČÍTKO ---
                OutlinedButton(
                    onClick = {
                        viewModel.onContinueAsGuestClick {
                            onLoginSuccess()
                        }
                    },
                    enabled = state !is LoginState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MyGreen),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MyGreen
                    )
                ) {
                    Text(
                        text = "Pokračovat jako Host",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Data budou uložena pouze v telefonu.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // --- 6. LOADING OVERLAY ---
            // Překryvná vrstva při načítání, aby to vypadalo profi
            if (state is LoginState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)), // Poloprůhledné pozadí
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = MyWhite,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MyGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Přihlašování...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MyBlack
                            )
                        }
                    }
                }
            }
        }
    }
}