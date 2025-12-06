package cz.tomasjanicek.bp.ui.screens.cycle.components

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.tomasjanicek.bp.ui.screens.cycle.CalendarDay
import cz.tomasjanicek.bp.ui.screens.cycle.DayType
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyWhite
import cz.tomasjanicek.bp.ui.theme.Pink80
import cz.tomasjanicek.bp.ui.theme.TagGreen
import cz.tomasjanicek.bp.ui.theme.TagPurple

@Composable
fun CalendarView(
    days: List<CalendarDay>,
    onDayClick: (Int) -> Unit
) {
    val dayHeaders = listOf("Po", "Út", "St", "Čt", "Pá", "So", "Ne")

    Column {
        // Hlavička s názvy dnů
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { header ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = header, fontWeight = FontWeight.Bold, color = MyBlack)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Mřížka kalendáře
        val rows = days.chunked(7)
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { day ->
                    DayCell(
                        day = day,
                        onClick = { onDayClick(day.day) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Pokud řádek není plný, doplníme prázdné buňky
                if (row.size < 7) {
                    repeat(7 - row.size) {
                        Box(modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (day.day == 0) {
        Box(modifier = modifier.aspectRatio(1f))
        return
    }

    // Určíme barvy a šířku borderu podle typu dne
    val (backgroundColor, borderColor, borderWidth) = when (day.type) {
        // ===== REÁLNÉ ZÁZNAMY – vybarvené kolečko =====
        DayType.MENSTRUATION -> Triple(
            MyPink,
            if (day.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
            if (day.isToday) 2.dp else 0.dp
        )

        DayType.OVULATION -> Triple(
            TagPurple.copy(alpha = 0.8f),
            if (day.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
            if (day.isToday) 2.dp else 0.dp
        )

        DayType.FERTILE -> Triple(
            TagGreen.copy(alpha = 0.7f),
            if (day.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
            if (day.isToday) 2.dp else 0.dp
        )

        // ===== PREDIKCE – průhledné kolečko, barevný okraj =====
        DayType.PREDICTED_MENSTRUATION -> Triple(
            Color.Transparent,
            Pink80,
            2.dp
        )

        DayType.PREDICTED_FERTILE -> Triple(
            Color.Transparent,
            TagGreen,
            2.dp
        )

        DayType.PREDICTED_OVULATION -> Triple(
            Color.Transparent,
            TagPurple,
            2.dp
        )

        // ===== Normální den =====
        DayType.NORMAL -> Triple(
            Color.Transparent,
            if (day.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
            if (day.isToday) 2.dp else 0.dp
        )
    }

    // Text: bílý jen na plném pozadí, jinak černý
    val textColor = if (backgroundColor != Color.Transparent) MyWhite else MyBlack

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.day.toString(),
            color = textColor,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}