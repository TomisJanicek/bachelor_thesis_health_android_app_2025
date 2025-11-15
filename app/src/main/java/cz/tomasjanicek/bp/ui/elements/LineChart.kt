package cz.tomasjanicek.bp.ui.elements

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// --- Model ---
data class ChartPoint(
    val xEpochMillis: Long,
    val y: Float
)

enum class ChartPeriod(val label: String) {
    DAY("Den"),
    WEEK("7 dní"),
    DAYS_30("30 dní"),
    YEAR("Rok")
}

// --- Pomocné funkce ---
private fun filterPointsByPeriod(
    points: List<ChartPoint>,
    period: ChartPeriod,
    nowEpochMillis: Long = System.currentTimeMillis()
): List<ChartPoint> {
    if (points.isEmpty()) return points
    val start = when (period) {
        ChartPeriod.DAY     -> nowEpochMillis - ChronoUnit.DAYS.duration.toMillis()
        ChartPeriod.WEEK    -> nowEpochMillis - ChronoUnit.DAYS.duration.toMillis() * 7
        ChartPeriod.DAYS_30 -> nowEpochMillis - ChronoUnit.DAYS.duration.toMillis() * 30
        ChartPeriod.YEAR    -> nowEpochMillis - ChronoUnit.DAYS.duration.toMillis() * 365
    }
    return points.filter { it.xEpochMillis in start..nowEpochMillis }
        .sortedBy { it.xEpochMillis }
}

private fun defaultXFormatter(zoneId: ZoneId, period: ChartPeriod): (Long) -> String {
    val pattern = when (period) {
        ChartPeriod.DAY     -> "HH:mm"
        ChartPeriod.WEEK    -> "d.M."
        ChartPeriod.DAYS_30 -> "d.M."
        ChartPeriod.YEAR    -> "LLL"
    }
    val fmt = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
    return { epoch -> fmt.format(Instant.ofEpochMilli(epoch)) }
}

private fun defaultYFormatter(): (Float) -> String = { v ->
    // max 2 desetinná místa, ale bez zbytečných nul
    if (v % 1f == 0f) v.toInt().toString() else String.format("%.2f", v)
}

// --- UI: Přepínač období (Material 3 segmented) ---
@Composable
fun PeriodSelector(
    selected: ChartPeriod,
    onSelected: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    SingleChoiceSegmentedButtonRow(modifier) {
        ChartPeriod.values().forEachIndexed { index, period ->
            SegmentedButton(
                selected = selected == period,
                onClick = { onSelected(period) },
                shape = SegmentedButtonDefaults.itemShape(index, ChartPeriod.values().size),
                colors = SegmentedButtonDefaults.colors(
                    // Barva pozadí pro VYBRANÉ tlačítko
                    activeContainerColor = colors.secondary,
                    // Barva textu a ikony pro VYBRANÉ tlačítko
                    activeContentColor = colors.onPrimary,
                    // Barva pozadí pro NEVYBRANÁ tlačítka
                    inactiveContainerColor = colors.surface,
                    // Barva textu a ikony pro NEVYBRANÁ tlačítka
                    inactiveContentColor = colors.onSurface
                )
            ) { Text(period.label) }
        }
    }
}

// --- Hlavní graf ---
@Composable
fun LineChart(
    points: List<ChartPoint>,
    period: ChartPeriod,
    modifier: Modifier = Modifier,
    minX: Long? = null,
    maxX: Long? = null,
    minY: Float? = null,
    maxY: Float? = null,
    padding: Dp = 12.dp,
    gridLines: Int = 4,
    zoneId: ZoneId = ZoneId.systemDefault(),
    xLabelFormatter: (Long) -> String = defaultXFormatter(ZoneId.systemDefault(), period),
    yLabelFormatter: (Float) -> String = defaultYFormatter()
) {
    val filtered = remember(points, period) { filterPointsByPeriod(points, period) }

    val xMin = minX ?: filtered.minOfOrNull { it.xEpochMillis } ?: 0L
    val xMax = maxX ?: filtered.maxOfOrNull { it.xEpochMillis } ?: (xMin + 1L)
    val yMin = minY ?: filtered.minOfOrNull { it.y } ?: 0f
    val yMax = maxY ?: filtered.maxOfOrNull { it.y } ?: (yMin + 1f)

    val safeXRange = max(1L, xMax - xMin)
    val safeYRange = max(1e-6f, yMax - yMin)

    val density = LocalDensity.current


    // animované „dokončení“ čáry (0..1)
    val drawProgress by animateFloatAsState(targetValue = if (filtered.isEmpty()) 0f else 1f, label = "lineProgress")

    // Odvozené popisky os (5 horizontálních/vertikálních dělení)
    val yTicks = List(gridLines + 1) { i -> yMin + i * (safeYRange / gridLines) }
    val xTicks: List<Long> = List(gridLines + 1) { i -> xMin + (safeXRange * i) / gridLines }

    val scheme = MaterialTheme.colorScheme
    val axisColor = scheme.outlineVariant
    val gridColor = scheme.outlineVariant.copy(alpha = 0.35f)
    val lineColor = scheme.primary
    val pointColor = scheme.primary

    Column(modifier) {
        // Osa + graf
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(padding)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Rezervy na popisky os
                val leftPad = 44f
                val bottomPad = 26f
                val topPad = 8f
                val rightPad = 12f

                val chartW = w - leftPad - rightPad
                val chartH = h - topPad - bottomPad

                fun mapX(xEpoch: Long): Float {
                    if (chartW <= 0f) return leftPad
                    val t = (xEpoch - xMin).toFloat() / safeXRange.toFloat()
                    return leftPad + t.coerceIn(0f, 1f) * chartW
                }
                fun mapY(y: Float): Float {
                    if (chartH <= 0f) return topPad + chartH
                    val t = (y - yMin) / safeYRange
                    // Y roste dolů, proto invert
                    return topPad + (1f - t.coerceIn(0f, 1f)) * chartH
                }

                // --- Mřížka horizontální + Y popisky
                yTicks.forEach { yTick ->
                    val yPx = mapY(yTick)
                    // grid line
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPad, yPx),
                        end = Offset(leftPad + chartW, yPx),
                        strokeWidth = 1f
                    )
                    // y label
                    drawContext.canvas.nativeCanvas.apply {
                        val txt = yLabelFormatter(yTick)
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            val textSizePx = with(density) { 10.sp.toPx() }
                            textSize = textSizePx
                            color = android.graphics.Color.argb(
                                (scheme.onSurfaceVariant.alpha * 255).toInt(),
                                (scheme.onSurfaceVariant.red * 255).toInt(),
                                (scheme.onSurfaceVariant.green * 255).toInt(),
                                (scheme.onSurfaceVariant.blue * 255).toInt()
                            )
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                        drawText(
                            txt,
                            leftPad - 6f,
                            yPx + (paint.textSize / 3f),
                            paint
                        )
                    }
                }

                // --- Osy
                val xAxisY = mapY(yMin)
                drawLine(
                    color = axisColor,
                    start = Offset(leftPad, xAxisY),
                    end = Offset(leftPad + chartW, xAxisY),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = axisColor,
                    start = Offset(leftPad, topPad),
                    end = Offset(leftPad, topPad + chartH),
                    strokeWidth = 1.5f
                )

                // --- Vertikální mřížka + X popisky
                xTicks.forEach { xTick ->
                    val xPx = mapX(xTick)
                    drawLine(
                        color = gridColor,
                        start = Offset(xPx, topPad),
                        end = Offset(xPx, topPad + chartH),
                        strokeWidth = 1f
                    )
                    // x label
                    drawContext.canvas.nativeCanvas.apply {
                        val txt = defaultXFormatter(ZoneId.systemDefault(), period)(xTick)
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            val textSizePx = with(density) { 10.sp.toPx() }
                            textSize = textSizePx
                            color = android.graphics.Color.argb(
                                (scheme.onSurfaceVariant.alpha * 255).toInt(),
                                (scheme.onSurfaceVariant.red * 255).toInt(),
                                (scheme.onSurfaceVariant.green * 255).toInt(),
                                (scheme.onSurfaceVariant.blue * 255).toInt()
                            )
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(
                            txt,
                            xPx,
                            topPad + chartH + paint.textSize + 4f,
                            paint
                        )
                    }
                }

                // --- Linie grafu
                if (filtered.size >= 2) {
                    val path = Path()
                    filtered.forEachIndexed { idx, p ->
                        val x = mapX(p.xEpochMillis)
                        val y = mapY(p.y)
                        if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    // animované "ořezání" čáry přes PathMeasure by bylo delší, tady zjednodušeně: kreslíme po segmentech
                    val lastIndex = (filtered.lastIndex * drawProgress).toInt().coerceAtLeast(1)
                    if (lastIndex >= 1) {
                        val animPath = Path()
                        for (i in 0..lastIndex) {
                            val p = filtered[i]
                            val x = mapX(p.xEpochMillis)
                            val y = mapY(p.y)
                            if (i == 0) animPath.moveTo(x, y) else animPath.lineTo(x, y)
                        }
                        drawPath(
                            path = animPath,
                            color = lineColor,
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )
                    }

                    // body (jemné)
                    filtered.forEach { p ->
                        val x = mapX(p.xEpochMillis)
                        val y = mapY(p.y)
                        drawCircle(
                            color = pointColor,
                            radius = 3.5f,
                            center = Offset(x, y)
                        )
                    }
                } else if (filtered.size == 1) {
                    // jediný bod
                    val p = filtered.first()
                    drawCircle(
                        color = pointColor,
                        radius = 4.5f,
                        center = Offset(mapX(p.xEpochMillis), mapY(p.y))
                    )
                }
            }
        }

        // Info řádek (volitelné)
        if (filtered.isEmpty()) {
            Text(
                "Pro zvolené období nejsou k dispozici žádná data.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
    }
}

// --- Ukázka použití ---
@Composable
fun LineChartWithControlsDemo(
    allPoints: List<ChartPoint>,
    modifier: Modifier = Modifier
) {
    var period by remember { mutableStateOf(ChartPeriod.DAYS_30) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = "Trend hodnot",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))

        PeriodSelector(
            selected = period,
            onSelected = { period = it
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            LineChart(
                points = allPoints,
                period = period,
                // min/max můžeš klidně vynechat -> auto
                // minX = ..., maxX = ..., minY = ..., maxY = ...,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


