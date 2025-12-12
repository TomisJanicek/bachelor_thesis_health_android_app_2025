package cz.tomasjanicek.bp.ui.screens.medicine.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MedicineReminderItem(
    reminder: MedicineReminder,
    medicine: Medicine?,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = reminder.completionDateTime != null
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val plannedTime = remember(reminder.plannedDateTime) {
        timeFormatter.format(Instant.ofEpochMilli(reminder.plannedDateTime).atZone(ZoneId.systemDefault()))
    }

    val textDecoration = if (isCompleted) TextDecoration.LineThrough else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MyGreen.copy(alpha = 0.3f) else MyGreen.copy(alpha = 0.8f),
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.secondary,
                    uncheckedColor = MyBlack
                )
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (medicine != null) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = textDecoration,
                            color = MyBlack
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dávka: ${medicine.dosage.toInt()} ${medicine.unit.label}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = textDecoration,
                            color = MyBlack
                        )
                    )
                    if (!medicine.note.isNullOrBlank()) {
                        Text(
                            text = "Pozn.: ${medicine.note}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = textDecoration,
                                color = MyBlack
                            ),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text("Načítání léku...", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = plannedTime,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Light,
                    textDecoration = textDecoration,
                    color = MyBlack
                ),
            )
        }
    }
}