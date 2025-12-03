package cz.tomasjanicek.bp.ui.screens.examination.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationStatus
import cz.tomasjanicek.bp.ui.elements.TagChip
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyRed
import cz.tomasjanicek.bp.utils.DateUtils

/**
 * Obsah pro "bottom sheet" zobrazující detail vyšetření.
 */
@Composable
fun ExaminationSheetContent(
    examination: Examination,
    onCompleteClick: (result: String) -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onRescheduleClick: () -> Unit, // Nová akce pro zrušené
    onCancelClick: (reason: String) -> Unit // <-- PŘIDAT TENTO ŘÁDEK
) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) } // <-- PŘIDAT TENTO ŘÁDEK

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp), // Symetrický padding
        horizontalAlignment = Alignment.Start
    ) {
        // --- HLAVNÍ INFORMACE ---
        TagChip(type = examination.type)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = examination.purpose,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MyBlack
            )
            Text(
                text = DateUtils.getDateTimeString(examination.dateTime),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // --- POZNÁMKA (pokud existuje) ---
        if (!examination.note.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Poznámka: ${examination.note}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
        // --- POZNÁMKA (pokud existuje) ---
        if (!examination.result.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Výsledek: ${examination.result}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider() // Vizuální oddělovač
        Spacer(modifier = Modifier.height(16.dp))

        // --- AKČNÍ TLAČÍTKA ---
        ActionButtons(
            status = examination.status,
            onCompleteClick = { showCompleteDialog = true },
            onDeleteClick = { showDeleteDialog = true },
            onEditClick = onEditClick,
            onRescheduleClick = onRescheduleClick,
            onCancelClick = { showCancelDialog = true } // <-- PŘIDAT TUTO AKCI
        )
    }

    // --- DIALOGY ---
    if (showCompleteDialog) {
        CompleteExaminationDialog(
            onDismiss = { showCompleteDialog = false },
            onConfirm = { result ->
                showCompleteDialog = false
                onCompleteClick(result)
            }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteClick()
            }
        )
    }

    if (showCancelDialog) {
        CancelExaminationDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = { reason ->
                showCancelDialog = false
                onCancelClick(reason)
            }
        )
    }
}

/**
 * Komponenta sdružující logiku pro zobrazení správných tlačítek
 * na základě stavu vyšetření (PLANNED, CANCELLED, atd.).
 */
@Composable
private fun ActionButtons(
    status: ExaminationStatus,
    onCompleteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onRescheduleClick: () -> Unit,
    onCancelClick: () -> Unit // <-- PŘIDAT PARAMETR
)  {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (status) {
            ExaminationStatus.PLANNED, ExaminationStatus.OVERDUE -> {
                // Hlavní akce pro naplánované vyšetření
                FilledTonalButton(
                    onClick = onCompleteClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MyBlack
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dokončit a zapsat výsledek")
                }
                // Sekundární akce
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = MyBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upravit", color = MyBlack)
                    }
                    TextButton(onClick = onCancelClick) {
                        Icon(
                            Icons.Filled.Cancel, // Lepší ikona než DoDisturb
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zrušit", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MyRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Smazat", color = MyRed)
                    }
                }
            }
            ExaminationStatus.CANCELLED -> {
                // Akce pro zrušené vyšetření
                FilledTonalButton(
                    onClick = onRescheduleClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Naplánovat znovu")
                }
                TextButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trvale smazat", color = MaterialTheme.colorScheme.error)
                }
            }
            // Pro COMPLETED stav nezobrazujeme žádná tlačítka
            ExaminationStatus.COMPLETED -> {
                Text("Toto vyšetření je již dokončeno.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Dialog pro potvrzení smazání.
 */
@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Opravdu smazat?") },
        text = { Text("Tato akce je nevratná. Záznam o vyšetření bude trvale odstraněn.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                // Barvy pro zdůraznění destruktivní akce
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Smazat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}


@Composable
fun CompleteExaminationDialog(
    onDismiss: () -> Unit,
    onConfirm: (result: String) -> Unit
) {
    var resultText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = { Text("Zapsat výsledek prohlídky") },
        text = {
            OutlinedTextField(
                value = resultText,
                onValueChange = { resultText = it },
                label = { Text("Výsledek...") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Např. 'Vše v pořádku, kontrola za rok'") }
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(resultText) },
                enabled = resultText.isNotBlank()
            ) {
                Text("Uložit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )
}
/**
* Dialog pro zadání důvodu zrušení prohlídky.
*/
@Composable
fun CancelExaminationDialog(
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reasonText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.QuestionMark, contentDescription = null) },
        title = { Text("Zrušit prohlídku") },
        text = {
            OutlinedTextField(
                value = reasonText,
                onValueChange = { reasonText = it },
                label = { Text("Důvod zrušení...") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Např. 'Nemoc, přeobjednám se'") },
                isError = reasonText.isBlank() // Zvýrazní, pokud je pole prázdné
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reasonText) },
                enabled = reasonText.isNotBlank() // Tlačítko je aktivní, jen když je zadán důvod
            ) {
                Text("Potvrdit zrušení")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zpět")
            }
        }
    )
}
