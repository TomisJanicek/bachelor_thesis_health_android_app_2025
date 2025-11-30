package cz.tomasjanicek.bp.ui.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.bp.model.ExaminationType
import cz.tomasjanicek.bp.ui.theme.getContrastingTextColor

@Composable
fun TagChip(type: ExaminationType) {
    // 1. Zavoláme naši novou funkci, abychom určili správnou barvu textu.
    val textColor = getContrastingTextColor(backgroundColor = type.tagColor)
    // Můžeme i určit barvu rámečku pro lepší kontrast
    val borderColor = getContrastingTextColor(
        backgroundColor = type.tagColor,
        lightColor = Color.White.copy(alpha = 0.5f),
        darkColor = Color.Black.copy(alpha = 0.5f)
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        //color = type.tagColor.copy(alpha = 0.5f), // jemné pozadí
        color = type.tagColor,
        border = BorderStroke(1.dp, Color.Black),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            /*
            // malý tečkový indikátor v barvě typu (volitelné)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(type.tagColor)
            )
            Spacer(Modifier.width(6.dp))
            */
            Text(
                text = type.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            )
        }
    }
}