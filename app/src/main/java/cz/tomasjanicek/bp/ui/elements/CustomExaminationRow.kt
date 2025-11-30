package cz.tomasjanicek.bp.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationType
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import coil.compose.AsyncImage
import cz.tomasjanicek.bp.model.Doctor // Doplň import
import cz.tomasjanicek.bp.model.ExaminationStatus
import java.time.Instant
import java.time.ZoneId

@Composable
fun CustomExaminationRow(
    item: ExaminationWithDoctor,
    onClick: () -> Unit = {}
) {
    val radius = 28.dp
    val cardShape = RoundedCornerShape(radius)
    val dateFormatter = DateTimeFormatter.ofPattern("d. M. yyyy - HH:mm")

    // 3-sloupcové rozvržení
    val LEFT_WEIGHT = 0.44f
    val GAP = 8.dp

    val examination = item.examination
    val doctor = item.doctor
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(examination.dateTime), ZoneId.systemDefault())

    // 3. KROK: Připravíme si styl pro přeškrtnutí, pokud je stav CANCELLED
    val textStyle = if (examination.status == ExaminationStatus.CANCELLED) {
        TextStyle(textDecoration = TextDecoration.LineThrough)
    } else {
        TextStyle()
    }

    Card(
        onClick = onClick,
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            // -------- 3 SLOUPCE --------
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp, vertical = 0.dp),
                verticalAlignment = Alignment.Top
            ) {
                // 1) OBRÁZEK – čtverec se stejným rádiusem jako karta
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(0.5f) // Zajistí čtvercový poměr
                        .clip(cardShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    // 4. KROK: Zobrazíme obrázek doktora, nebo jeho iniciály
                    if (!doctor.image.isNullOrBlank()) {
                        // Pokud má doktor URL na obrázek, použijeme Coil k jeho zobrazení
                        AsyncImage(
                            model = doctor.image,
                            contentDescription = "Fotografie lékaře: ${doctor.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Pokud obrázek nemá, zobrazíme první písmeno jeho jména
                        Text(
                            text = doctor.specialization?.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 2) MEZERA
                Spacer(Modifier.width(GAP))

                // 3) TEXTY + ŠTÍTEK
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(cardShape)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primary),
                    verticalArrangement = Arrangement.Top
                ) {
                    // První řádek: Nadpis + štítek vpravo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 16.dp, 16.dp, 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = doctor.specialization,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ).merge(textStyle), // <-- PŘIDÁNO ZDE
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))

                    }

                    if (!examination.purpose.isNullOrBlank()) {
                        Text(
                            text = examination.purpose,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }


                    Spacer(modifier = Modifier.weight(1f)) // 🔥 vyplní zbytek výšky dynamicky

                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.width(GAP))
                        Text(
                            text = dateTime.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp,0.dp, 16.dp, 16.dp)) {
                        TagChip(examination.type) // viz níže
                    }
                }
            }
        }
    }
}

// 6. KROK: Opravíme i náhled, aby fungoval se správnými daty
@Preview(showBackground = true, backgroundColor = 0xFFF2EEF2, widthDp = 380)
@Composable
private fun PreviewCustomExaminationRow() {
    // Vytvoříme si falešná data pro náhled
    val previewDoctor = Doctor(id = 1, name = "MUDr. Jan Novák", specialization = "Praktický lékař")
    val previewExamination = Examination(id = 1, doctorId = 1, purpose = "Preventivní prohlídka", dateTime = System.currentTimeMillis(), type = ExaminationType.PROHLIDKA)
    val previewItem = ExaminationWithDoctor(examination = previewExamination, doctor = previewDoctor)

    MaterialTheme(colorScheme = lightColorScheme()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            // Normální stav
            CustomExaminationRow(item = previewItem)

            // Přeškrtnutý stav (CANCELLED)
            val cancelledItem = previewItem.copy(
                examination = previewItem.examination.copy(status = ExaminationStatus.CANCELLED)
            )
            CustomExaminationRow(item = cancelledItem)
        }
    }
}