package cz.tomasjanicek.bp.ui.screens.examination.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationStatus
import cz.tomasjanicek.bp.ui.elements.TagChip
import cz.tomasjanicek.bp.ui.screens.examination.detail.toCzechString
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyRed
import cz.tomasjanicek.bp.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExaminationItemCard(
    examination: Examination,
    onAddToCalendar: (Examination) -> Unit,
    onItemClick: (Examination) -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(examination.dateTime) {
        DateUtils.getDateString(examination.dateTime)
    }
    val formattedTime = remember(examination.dateTime) {
        DateUtils.getTimeString(examination.dateTime)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray.copy(alpha = 0.1f),


            ),
        onClick = { onItemClick(examination) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (examination.status == ExaminationStatus.OVERDUE) MyRed else MyBlack                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (examination.status == ExaminationStatus.OVERDUE) MyRed else MyBlack                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = examination.purpose,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MyBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    TagChip(type = examination.type)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = examination.status.toCzechString().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tlačítko kalendáře se zobrazí jen pro budoucí plánované prohlídky
            if (examination.status == ExaminationStatus.PLANNED) {
                IconButton(
                    onClick = { onAddToCalendar(examination) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MyBlack)
                ) {
                    // DOČASNĚ POUŽÍVÁM STANDARDNÍ IKONU
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Přidat do kalendáře"
                    )
                }
            }
        }
    }
}