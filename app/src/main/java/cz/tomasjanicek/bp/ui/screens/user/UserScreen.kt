package cz.tomasjanicek.bp.ui.screens.user

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import cz.tomasjanicek.bp.navigation.INavigationRouter

@Composable
fun UserScreen(
    navigationRouter: INavigationRouter,
    viewModel: UserViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val isSigningOut by viewModel.isSigningOut.collectAsState()
    val isGuest by viewModel.isGuest.collectAsState()
    val context = LocalContext.current

    // --- MAGIE PRO ZÍSKÁNÍ OPRÁVNĚNÍ K DISKU ---
    // Toto zachytí výsledek, až uživatel odklikne "Povolit přístup k Disku"
    val googlePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Ať už to dopadlo jakkoliv (OK nebo Zrušeno), spustíme proces odhlášení.
        // Pokud dal OK, repository uvnitř signOut() bude mít oprávnění a záloha projde.
        // Pokud to zrušil, záloha selže, ale odhlášení proběhne (díky naší záchranné brzdě).
        viewModel.onSignOutClick {
            navigationRouter.navigateToLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Profilová fotka
        if (isGuest) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Guest",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            AsyncImage(
                model = user?.photoUrl,
                contentDescription = "Profilová fotka",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Jméno
        if (isGuest) {
            Text(
                text = "Lokální účet (Host)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = user?.displayName ?: "Uživatel",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Info texty
        if (isGuest) {
            Text(
                text = "Data jsou uložena pouze v tomto zařízení.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Text(
                text = "Ukončením režimu hosta o data přijdete!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Text(
                text = "Při odhlášení se pokusíme data zálohovat.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 4. Tlačítko
        Button(
            onClick = {
                if (isGuest) {
                    // Host nepotřebuje zálohovat na Disk -> rovnou mažeme
                    viewModel.onSignOutClick {
                        navigationRouter.navigateToLogin()
                    }
                } else {
                    // --- UŽIVATEL S GOOGLE ÚČTEM ---
                    // Než ho odhlásíme, musíme si vyžádat oprávnění pro Drive (pokud ho ještě nemá).
                    // Spustíme Google Sign-In intent s DRIVE scope.
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA)) // <-- Chceme Disk
                        .build()
                    val client = GoogleSignIn.getClient(context, gso)

                    // Spustíme intent -> výsledek zachytí 'googlePermissionsLauncher' nahoře
                    googlePermissionsLauncher.launch(client.signInIntent)
                }
            },
            enabled = !isSigningOut,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSigningOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Zálohuji a odhlašuji...")
            } else {
                Text(if (isGuest) "Ukončit režim a smazat data" else "Odhlásit se")
            }
        }
    }
}