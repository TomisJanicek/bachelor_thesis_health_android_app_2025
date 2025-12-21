package cz.tomasjanicek.bp.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cz.tomasjanicek.bp.model.ExaminationNotificationTime
import cz.tomasjanicek.bp.model.MedicineNotificationTime
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.bottomBar.AppSection
import cz.tomasjanicek.bp.ui.theme.AppTheme
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyRed
import cz.tomasjanicek.bp.ui.theme.MyWhite

// --- SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigationRouter: INavigationRouter,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val enabledSet by viewModel.enabledSections.collectAsState(initial = emptySet())

    // State pro dialog potvrzení smazání
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialog pro potvrzení smazání dat
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Smazat všechna data?") },
            text = {
                Text("Tato akce je nevratná. Všechny vaše záznamy (léky, prohlídky, měření) budou trvale odstraněny.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyRed)
                ) {
                    Text("Smazat", color = MyWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Zrušit")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Nastavení", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navigationRouter.returBack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Zpět")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // --- 1. SEKCE: VZHLED ---
            Text(
                text = "Vzhled aplikace",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ThemeOptionItem(
                        title = "Podle systému",
                        description = "Automaticky přepínat",
                        icon = Icons.Default.SettingsBrightness,
                        isSelected = currentTheme == AppTheme.SYSTEM,
                        onClick = { viewModel.onThemeSelected(AppTheme.SYSTEM) }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ThemeOptionItem(
                        title = "Světlý režim",
                        description = "Vždy světlé barvy",
                        icon = Icons.Default.LightMode,
                        isSelected = currentTheme == AppTheme.LIGHT,
                        onClick = { viewModel.onThemeSelected(AppTheme.LIGHT) }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ThemeOptionItem(
                        title = "Tmavý režim",
                        description = "Šetří baterii a zrak",
                        icon = Icons.Default.DarkMode,
                        isSelected = currentTheme == AppTheme.DARK,
                        onClick = { viewModel.onThemeSelected(AppTheme.DARK) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 2. SEKCE: UPOZORNĚNÍ ---
            Text(
                text = "Upozornění",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Načítáme stavy z ViewModelu
                    val notifEnabled by viewModel.notificationsEnabled.collectAsState()
                    val medTime by viewModel.medicineNotifTime.collectAsState()
                    val examTime by viewModel.examNotifTime.collectAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Povolit upozornění",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = notifEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MyWhite,
                                checkedTrackColor = MyGreen,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    if (notifEnabled) {
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Nastavení pro Léky
                        NotificationSelectorItem(
                            label = "Připomenutí léků",
                            currentValue = medTime.label,
                            options = MedicineNotificationTime.values().toList(),
                            onOptionSelected = { viewModel.setMedicineTime(it) },
                            labelSelector = { it.label }
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Nastavení pro Prohlídky
                        NotificationSelectorItem(
                            label = "Připomenutí prohlídek",
                            currentValue = examTime.label,
                            options = ExaminationNotificationTime.values().toList(),
                            onOptionSelected = { viewModel.setExamTime(it) },
                            labelSelector = { it.label }
                        )

                        // --- TLAČÍTKO PRO TEST NOTIFIKACE ---
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        OutlinedButton(
                            onClick = { viewModel.sendTestNotification() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vyzkoušet notifikaci (za 5s)")
                        }

                        Text(
                            text = "Klikněte a zamkněte telefon pro test zamykací obrazovky.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. SEKCE: ZOBRAZENÉ MODULY ---
            Text(
                text = "Zobrazené moduly",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val toggleableSections = AppSection.values().filter { it.canBeDisabled }
                    toggleableSections.forEachIndexed { index, section ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Switch(
                                checked = section in enabledSet,
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleSection(section, isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MyWhite,
                                    checkedTrackColor = MyGreen,
                                    checkedBorderColor = MyGreen,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                        if (index < toggleableSections.size - 1) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            // --- 4. SEKCE: SPRÁVA DAT (NOVÉ) ---
            Text(
                text = "Správa dat",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary, // Nebo MyRed, pokud chcete zdůraznit nebezpečí
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vymazat data",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Smaže vše kromě nastavení.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Smazat")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


// --- KOMPONENTY ---
@Composable
fun ThemeOptionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MyGreen.copy(alpha = 0.15f) else Color.Transparent, label = "bgColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MyGreen else MaterialTheme.colorScheme.onSurface, label = "textColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if(isSelected) MyGreen else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if(isSelected) MyWhite else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = contentColor)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = if (isSelected) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isSelected) {
            Box(modifier = Modifier.size(20.dp).border(2.dp, MyGreen, RoundedCornerShape(10.dp)).padding(4.dp).background(MyGreen, RoundedCornerShape(10.dp)))
        } else {
            Box(modifier = Modifier.size(20.dp).border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp)))
        }
    }
}

@Composable
fun <T> NotificationSelectorItem(
    label: String,
    currentValue: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    labelSelector: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(labelSelector(option)) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}