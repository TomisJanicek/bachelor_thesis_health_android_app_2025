package cz.tomasjanicek.bp.ui.screens.user

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import cz.tomasjanicek.bp.navigation.INavigationRouter

@Composable
fun UserScreen(
    navigationRouter: INavigationRouter,
    viewModel: UserViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val isSigningOut by viewModel.isSigningOut.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Profilová fotka
        // Google vrací URL fotky v user?.photoUrl
        AsyncImage(
            model = user?.photoUrl,
            contentDescription = "Profilová fotka",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape) // Oříznutí do kruhu
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Jméno
        Text(
            text = user?.displayName ?: "Neznámý uživatel",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Email
        Text(
            text = user?.email ?: "Email nedostupný",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 4. Tlačítko Odhlásit
        Button(
            onClick = {
                viewModel.onSignOutClick {
                    navigationRouter.navigateToLogin()
                }
            },
            // Pokud se odhlašuje, tlačítko zakážeme, aby nešlo klikat víckrát
            enabled = !isSigningOut,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSigningOut) {
                // Pokud pracujeme, ukážeme malé kolečko
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                // Jinak ukážeme text
                Text("Odhlásit se")
            }
        }
    }
}