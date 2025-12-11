package cz.tomasjanicek.bp.ui.elements

import androidx.compose.animation.core.copy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.bp.model.Injection
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.utils.DateUtils

@Composable
fun CustomInjectionRow(
    item: Injection,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        // Použijeme lehce odlišnou barvu pro vizuální odlišení od prohlídek
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        ),
        modifier = Modifier.height(112.dp) // Sjednotíme výšku s CustomExaminationRow
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1) IKONA OČKOVÁNÍ
            Icon(
                imageVector = Icons.Outlined.Vaccines,
                contentDescription = "Očkování",
                modifier = Modifier.size(40.dp), // Větší ikona místo avataru
                tint = MyBlack
            )

            Spacer(Modifier.width(16.dp))

            // 2) TEXTOVÁ ČÁST
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Horní řádek: Název vakcíny a kategorie
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MyBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Název nemoci, proti které se očkuje
                Text(
                    text = "Proti: ${item.disease}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f)) // Dynamická mezera

                // Spodní řádek: Ikona a datum
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Datum
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = "Datum",
                            modifier = Modifier.size(20.dp),
                            tint = MyBlack
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = DateUtils.getDateTimeString(item.date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MyBlack,
                        )
                    }
                }
            }
        }
    }
}