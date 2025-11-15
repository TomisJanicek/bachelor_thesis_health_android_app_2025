package cz.tomasjanicek.bp.ui.elements

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.tomasjanicek.bp.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Barevná paleta z tvého UI (mírně upravené hex)
private val Mint = Color(0xFFA8CCC4)         // hlavní
private val my_primary = Color(0xFFE0B0B0)    // světlejší levý panel
private val my_second = Color(0xFFA6D8BE)
private val TagYellow = Color(0xFFFFEB3B)    // štítek "Prohlídka"
private val TagStroke = Color(0xFF1A1A1A)

enum class ExaminationType(val label: String, val tagColor: Color = TagYellow) {
    PROHLIDKA("Prohlídka"),
    ZAKROK("Zákrok", Color(0xFF6CFF6C)),
    VYSETRENI("Vyšetření", Color(0xFFFF6B6B)),
    ODBER_KRVE("Odběr krve", Color(0xFFFF8AC0))
}

enum class ExaminationStatus { PLANNED, COMPLETED, CANCELLED }

@Composable
fun CustomExaminationRow(
    title: String,
    subtitle: String?,
    dateTime: LocalDateTime,
    type: ExaminationType,
    status: ExaminationStatus = ExaminationStatus.PLANNED,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
    weight: Dp = 140.dp,
    onClick: () -> Unit = {}
) {
    val radius = 28.dp
    val cardShape = RoundedCornerShape(radius)
    val dateFormatter = DateTimeFormatter.ofPattern("d. M. yyyy - HH:mm")

    // 3-sloupcové rozvržení
    val LEFT_WEIGHT = 0.44f
    val GAP = 8.dp

    Card(
        onClick = onClick,
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = my_second.copy(alpha = 0.5f) // <-- TADY JE ZMĚNA
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
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
                        .aspectRatio(1f)
                        .clip(cardShape)
                        .background(my_second), // DEBUG podklad (klidně pak smaž)
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // 2) MEZERA
                Spacer(Modifier.width(GAP))

                // 3) TEXTY + ŠTÍTEK
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(cardShape)
                        .weight(1f)
                        .background(my_second),
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
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF222222),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))

                    }

                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF4B4B4B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = modifier.padding(horizontal = 16.dp)
                        )
                    }


                    Spacer(modifier = Modifier.weight(1f)) // 🔥 vyplní zbytek výšky dynamicky

                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier.padding(horizontal = 16.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF1F1F1F)
                        )
                        Spacer(Modifier.width(GAP))
                        Text(
                            text = dateTime.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2C2C2C),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier.padding(16.dp,0.dp, 16.dp, 16.dp)) {
                        TagChip(type) // viz níže
                    }
                }
            }
        }
    }
}

// Malý komponent pro štítek v textové části
@Composable
private fun TagChip(type: ExaminationType) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = type.tagColor.copy(alpha = 0.5f), // jemné pozadí
        border = BorderStroke(1.dp, type.tagColor),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // malý tečkový indikátor v barvě typu (volitelné)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(type.tagColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = type.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000)
                )
            )
        }
    }
}





@Preview(showBackground = true, backgroundColor = 0xFFF2EEF2, widthDp = 380, heightDp = 140)
@Composable
private fun PreviewCustomExaminationRow() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        CustomExaminationRow(
            title = "Praktik neuronů",
            subtitle = "Jdu preventivně a protože se fakt bojím tak nevím co s tím",
            dateTime = LocalDateTime.of(2025, 12, 25, 22, 22),
            type = ExaminationType.PROHLIDKA,
            iconRes = R.drawable.ic_launcher_foreground // nahraď svým assetem
        )
    }
}