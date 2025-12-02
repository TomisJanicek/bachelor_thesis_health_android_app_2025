package cz.tomasjanicek.bp.ui.elements

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.utils.DateUtils
import java.time.Instant
import java.time.ZoneId

@Composable
fun CustomExaminationRow(
    item: ExaminationWithDoctor,
    onClick: () -> Unit = {}
) {
    val examination = item.examination
    val doctor = item.doctor
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(examination.dateTime), ZoneId.systemDefault())
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        ),
        modifier = Modifier.height(112.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1) AVATAR (kolečko)
            DoctorAvatar(doctor = doctor)

            Spacer(Modifier.width(16.dp))

            // 2) TEXTOVÁ ČÁST
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Horní řádek: Specializace a Datum
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = doctor?.specialization ?: "Neznámá specializace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier,
                            textDecoration = if (examination.status == ExaminationStatus.CANCELLED) TextDecoration.LineThrough else null
                        )

                        if (!examination.purpose.isNullOrBlank()) {
                            Text(
                                text = examination.purpose,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier,
                                textDecoration = if (examination.status == ExaminationStatus.CANCELLED) TextDecoration.LineThrough else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f)) // Dynamická mezera

                // Spodní řádek: Ikona, datum a štítek
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
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
                            text = DateUtils.getDateTimeString(examination.dateTime),                           style = MaterialTheme.typography.bodyMedium,
                            color = MyBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (examination.status == ExaminationStatus.CANCELLED) TextDecoration.LineThrough else null
                        )
                    }
                    TagChip(examination.type)
                }
            }
        }
    }
}

@Composable
fun DoctorAvatar(doctor: Doctor?) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        val imageName = doctor?.image

        if (!imageName.isNullOrBlank()) {
            val context = LocalContext.current
            // Převedeme název obrázku (String) na jeho ID (Int)
            val imageResId = remember(imageName) {
                context.resources.getIdentifier(imageName, "drawable", context.packageName)
            }

            // Pokud bylo ID úspěšně nalezeno, zobrazíme obrázek
            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "Fotografie lékaře: ${doctor.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Pojistka: Pokud obrázek v drawable chybí, zobrazíme iniciálu
                AvatarInitial(doctor = doctor)
            }
        } else {
            // Pokud v datech není žádný obrázek, zobrazíme iniciálu
            AvatarInitial(doctor = doctor)
        }
    }
}

// Vytvoříme pomocnou komponentu pro zobrazení iniciály, abychom se neopakovali
@Composable
private fun AvatarInitial(doctor: Doctor?) {
    Text(
        text = doctor?.specialization?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
        style = MaterialTheme.typography.headlineSmall,
        color = MyBlack
    )
}