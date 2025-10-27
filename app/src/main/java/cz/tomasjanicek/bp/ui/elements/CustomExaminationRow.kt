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
private val MintLight = Color(0xFFBDD9D3)    // světlejší levý panel
private val TagYellow = Color(0xFFFFDD66)    // štítek "Prohlídka"
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
    onClick: () -> Unit = {}
) {
    val radius = 28.dp
    val cardShape = RoundedCornerShape(radius)
    val dateFormatter = DateTimeFormatter.ofPattern("d. M. yyyy - HH:mm")

    // přesný poměr levé části vůči celé kartě
    val leftFraction = 0.44f      // dolaď klidně 0.42–0.46 podle Figmy

    Card(
        onClick = onClick,
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Mint), // základ = tmavší mint (LELÝ panel)
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Box(Modifier.fillMaxSize()) {

            // 1) PRAVÝ PANEL se zakulaceným levým okrajem -> tvoří "hezké zakulacené oddělení"
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(1f) // start: celé
                    .clip(cardShape)
                    .background(MintLight) // světlejší pravá část
                    .align(Alignment.CenterEnd)
            )
            // Překreslit levý díl tmavší barvou tak, aby zůstal zakulacený střed
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(leftFraction)
                    .clip(cardShape)
                    .background(Mint) // vrátíme zpět tmavší L panel
                    .align(Alignment.CenterStart)
            )

            // 2) OBSAH – přesný layout: levý obrazový panel = čtverec přes CELÝ levý díl
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Obrázek přes celý levý panel, se stejným rádiusem
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(leftFraction)
                        .clip(cardShape)
                        .background(Color(0xFFBBD5FF)), // pomocné modré pozadí (můžeš odstranit)
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop // čtverec vyplní celý panel
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF222222),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!subtitle.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF4B4B4B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF1F1F1F)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = dateTime.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF2C2C2C),
                            maxLines = 1,
                            softWrap = false,             // nezalamuj
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 3) Rohový štítek
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(topEnd = 24.dp, bottomStart = 24.dp))
                    .background(type.tagColor)
                    .border(
                        BorderStroke(2.dp, TagStroke),
                        RoundedCornerShape(topEnd = 24.dp, bottomStart = 24.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1A1A1A)
                )
            }
        }
    }
}




@Preview(showBackground = true, backgroundColor = 0xFFF2EEF2, widthDp = 380, heightDp = 160)
@Composable
private fun PreviewCustomExaminationRow() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        CustomExaminationRow(
            title = "Praktik",
            subtitle = "Jdu preventivně",
            dateTime = LocalDateTime.of(2025, 12, 25, 22, 22),
            type = ExaminationType.PROHLIDKA,
            iconRes = R.drawable.ic_launcher_foreground // nahraď svým assetem
        )
    }
}