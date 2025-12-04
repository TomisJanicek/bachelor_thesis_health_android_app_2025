package cz.tomasjanicek.bp.ui.elements

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyWhite
import kotlin.math.max
import kotlin.math.min
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import kotlin.math.abs

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
private fun processPointsForPeriod(
    points: List<ChartPoint>,
    period: ChartPeriod,
    nowEpochMillis: Long = System.currentTimeMillis()
): List<ChartPoint> {
    if (points.isEmpty()) return emptyList()

    val start = when (period) {
        ChartPeriod.DAY -> nowEpochMillis - ChronoUnit.DAYS.duration.toMillis()
        ChartPeriod.WEEK -> nowEpochMillis - ChronoUnit.DAYS.duration.toMillis() * 7
        ChartPeriod.DAYS_30 -> nowEpochMillis - ChronoUnit.DAYS.duration.toMillis() * 30
        ChartPeriod.YEAR -> nowEpochMillis - ChronoUnit.DAYS.duration.toMillis() * 365
    }

    val relevantPoints = points.filter { it.xEpochMillis in start..nowEpochMillis }

    return when (period) {
        // Pro krátká období vracíme všechny body
        ChartPeriod.DAY, ChartPeriod.WEEK -> {
            relevantPoints.sortedBy { it.xEpochMillis }
        }

        // Pro 30 dní agregujeme po týdnech, jen pokud je bodů hodně
        ChartPeriod.DAYS_30 -> {
            if (relevantPoints.size < 15) {
                return relevantPoints.sortedBy { it.xEpochMillis }
            }
            relevantPoints
                .groupBy {
                    val instant = Instant.ofEpochMilli(it.xEpochMillis)
                    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
                    "${zonedDateTime.year}-${zonedDateTime.get(WeekFields.ISO.weekOfYear())}"
                }
                .map { (_, pointsInGroup) ->
                    val avgY = pointsInGroup.map { it.y }.average().toFloat()
                    val avgX = pointsInGroup.map { it.xEpochMillis }.average().toLong()
                    ChartPoint(xEpochMillis = avgX, y = avgY)
                }
                .sortedBy { it.xEpochMillis }
        }

        // Pro Rok agregujeme po měsících
        ChartPeriod.YEAR -> {
            relevantPoints
                .groupBy {
                    val instant = Instant.ofEpochMilli(it.xEpochMillis)
                    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
                    "${zonedDateTime.year}-${zonedDateTime.monthValue}"
                }
                .map { (_, pointsInGroup) ->
                    val avgY = pointsInGroup.map { it.y }.average().toFloat()
                    val firstDayOfMonth = Instant.ofEpochMilli(pointsInGroup.first().xEpochMillis)
                        .atZone(ZoneId.systemDefault()).withDayOfMonth(15).toInstant().toEpochMilli()
                    ChartPoint(xEpochMillis = firstDayOfMonth, y = avgY)
                }
                .sortedBy { it.xEpochMillis }
        }
    }
}


private fun defaultXFormatter(zoneId: ZoneId, period: ChartPeriod): (Long) -> String {
    val pattern = when (period) {
        ChartPeriod.DAY -> "HH:mm"
        ChartPeriod.WEEK -> "d.M."
        ChartPeriod.DAYS_30 -> "d.M."
        ChartPeriod.YEAR -> "LLL"
    }
    val fmt = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
    return { epoch -> fmt.format(Instant.ofEpochMilli(epoch)) }
}

private fun defaultYFormatter(): (Float) -> String = { v ->
    if (v % 1f == 0f) v.toInt().toString() else String.format("%.2f", v)
}

// --- UI: Přepínač období ---
@OptIn(ExperimentalMaterial3Api::class)
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
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ChartPeriod.values().size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = colors.secondary,
                    activeContentColor = MyBlack,
                    activeBorderColor = MyBlack,
                    inactiveContainerColor = MyWhite,
                    inactiveContentColor = MyBlack,
                    inactiveBorderColor = MyBlack
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
    yLimitMin: Float? = null,
    yLimitMax: Float? = null,
    padding: Dp = 12.dp,
    gridLines: Int = 4,
    zoneId: ZoneId = ZoneId.systemDefault(),
    yLabelFormatter: (Float) -> String = defaultYFormatter()
) {
    val filtered = remember(points, period) { processPointsForPeriod(points, period) }

    val (yMin, yMax) = remember(filtered, yLimitMin, yLimitMax) {
        val dataMinY = filtered.minOfOrNull { it.y }
        val dataMaxY = filtered.maxOfOrNull { it.y }

        var finalMin = min(dataMinY ?: yLimitMin ?: 0f, yLimitMin ?: Float.POSITIVE_INFINITY)
        var finalMax = max(dataMaxY ?: yLimitMax ?: 1f, yLimitMax ?: Float.NEGATIVE_INFINITY)

        val yPadding = (finalMax - finalMin) * 0.15f
        finalMin -= yPadding
        finalMax += yPadding

        if (finalMin.isInfinite() || finalMax.isInfinite() || finalMin == finalMax) {
            finalMin = (dataMinY ?: 0f) - 5f
            finalMax = (dataMaxY ?: 1f) + 5f
        }
        if (finalMin == finalMax) {
            finalMin -= 1f
            finalMax += 1f
        }
        finalMin to finalMax
    }

    val now = System.currentTimeMillis()
    val xMin = when (period) {
        ChartPeriod.DAY -> now - ChronoUnit.DAYS.duration.toMillis()
        ChartPeriod.WEEK -> now - ChronoUnit.DAYS.duration.toMillis() * 7
        ChartPeriod.DAYS_30 -> now - ChronoUnit.DAYS.duration.toMillis() * 30
        ChartPeriod.YEAR -> now - ChronoUnit.DAYS.duration.toMillis() * 365
    }
    val xMax = now

    val safeXRange = max(1L, xMax - xMin)
    val safeYRange = max(1e-6f, yMax - yMin)

    val density = LocalDensity.current
    val drawProgress by animateFloatAsState(targetValue = if (filtered.isEmpty()) 0f else 1f, label = "lineProgress")

    val yTicks = List(gridLines + 1) { i -> yMin + i * (safeYRange / gridLines) }
    val xTicks = List(gridLines + 1) { i -> xMin + (safeXRange * i) / gridLines }

    val scheme = MaterialTheme.colorScheme
    val gridColor = scheme.outlineVariant.copy(alpha = 0.35f)
    val lineColor = scheme.primary
    val pointColor = scheme.primary
    val limitLineColor = scheme.error
    val safeRangeColor = scheme.primary.copy(alpha = 0.05f)
    val labelColor = scheme.onSurfaceVariant
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    val highlightPaint = remember(density, limitLineColor) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = with(density) { 10.sp.toPx() }
            textAlign = android.graphics.Paint.Align.RIGHT
            color = limitLineColor.toArgb()
        }
    }

    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(padding)
        ) {
            if (filtered.isNotEmpty()) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val leftPad = 48f
                    val bottomPad = 32f
                    val topPad = 12f
                    val rightPad = 12f
                    val chartW = w - leftPad - rightPad
                    val chartH = h - topPad - bottomPad

                    // --- NOVÁ, TŘÍÚROVŇOVÁ LOGIKA PRO DYNAMICKÝ STYL ---
                    val (pointRadius, strokeWidth, isDetailedStyle) = when(period) {
                        ChartPeriod.DAY -> Triple(8f, 5f, true)
                        ChartPeriod.WEEK -> Triple(6f, 4f, true)
                        else -> Triple(3.5f, 3f, false)
                    }

                    fun mapX(xEpoch: Long): Float {
                        if (chartW <= 0f) return leftPad
                        val t = (xEpoch - xMin).toFloat() / safeXRange.toFloat()
                        return leftPad + t.coerceIn(0f, 1f) * chartW
                    }

                    fun mapY(y: Float): Float {
                        if (chartH <= 0f) return topPad + chartH
                        val t = (y - yMin) / safeYRange
                        return topPad + (1f - t.coerceIn(0f, 1f)) * chartH
                    }

                    // Mřížka + Y popisky
                    yTicks.forEach { yTick ->
                        val yPx = mapY(yTick)
                        drawLine(color = gridColor, start = Offset(leftPad, yPx), end = Offset(leftPad + chartW, yPx), strokeWidth = 1f)
                        drawContext.canvas.nativeCanvas.apply {
                            val txt = yLabelFormatter(yTick)
                            val paint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                textSize = with(density) { 10.sp.toPx() }
                                color = labelColor.toArgb()
                                textAlign = android.graphics.Paint.Align.RIGHT
                            }
                            drawText(txt, leftPad - 8f, yPx + (paint.textSize / 3f), paint)
                        }
                    }

                    // Zvýrazněné popisky pro MIN a MAX
                    if (yLimitMin != null) {
                        val yPx = mapY(yLimitMin)
                        val txt = yLabelFormatter(yLimitMin)
                        drawContext.canvas.nativeCanvas.drawText(txt, leftPad - 8f, yPx + (highlightPaint.textSize / 3f), highlightPaint)
                    }

                    if (yLimitMax != null) {
                        val yPx = mapY(yLimitMax)
                        val txt = yLabelFormatter(yLimitMax)
                        drawContext.canvas.nativeCanvas.drawText(txt, leftPad - 8f, yPx + (highlightPaint.textSize / 3f), highlightPaint)
                    }

                    // Mřížka + X popisky
                    xTicks.forEach { xTick ->
                        val xPx = mapX(xTick)
                        drawLine(color = gridColor, start = Offset(xPx, topPad), end = Offset(xPx, topPad + chartH), strokeWidth = 1f)
                        drawContext.canvas.nativeCanvas.apply {
                            val txt = defaultXFormatter(ZoneId.systemDefault(), period)(xTick)
                            val paint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                textSize = with(density) { 10.sp.toPx() }
                                color = labelColor.toArgb()
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            drawText(txt, xPx, topPad + chartH + paint.textSize + 6f, paint)
                        }
                    }

                    // Oblast "bezpečného" rozsahu
                    if (yLimitMin != null && yLimitMax != null) {
                        val y1 = mapY(yLimitMin)
                        val y2 = mapY(yLimitMax)
                        drawRect(brush = SolidColor(safeRangeColor), topLeft = Offset(leftPad, min(y1, y2)), size = Size(chartW, abs(y1 - y2)))
                    }

                    // Linie grafu
                    if (filtered.size >= 2) {
                        val path = Path()
                        filtered.forEachIndexed { idx, p ->
                            val x = mapX(p.xEpochMillis)
                            val y = mapY(p.y)
                            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        val lastIndex = (filtered.lastIndex * drawProgress).toInt().coerceAtLeast(1)
                        if (lastIndex >= 1) {
                            val animPath = Path()
                            for (i in 0..lastIndex) {
                                val p = filtered[i]
                                val x = mapX(p.xEpochMillis)
                                val y = mapY(p.y)
                                if (i == 0) animPath.moveTo(x, y) else animPath.lineTo(x, y)
                            }
                            drawPath(path = animPath, color = lineColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                        }
                        // Vykreslení bodů
                        filtered.forEach { p ->
                            val x = mapX(p.xEpochMillis)
                            val y = mapY(p.y)
                            drawCircle(color = pointColor, radius = pointRadius, center = Offset(x, y))
                            if (isDetailedStyle) {
                                // Použijeme barvu pozadí karty pro "dutý" efekt
                                drawCircle(color = cardBackgroundColor, radius = pointRadius - (strokeWidth / 2.5f), center = Offset(x, y))
                            }
                        }
                    } else if (filtered.size == 1) {
                        val p = filtered.first()
                        val x = mapX(p.xEpochMillis)
                        val y = mapY(p.y)
                        drawCircle(color = pointColor, radius = pointRadius, center = Offset(x,y))
                        if (isDetailedStyle) {
                            // Použijeme barvu pozadí karty pro "dutý" efekt
                            drawCircle(color = cardBackgroundColor, radius = pointRadius - (strokeWidth / 2.5f), center = Offset(x, y))
                        }
                    }

                    // Čáry pro limity
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    if (yLimitMin != null) {
                        val yPx = mapY(yLimitMin)
                        drawLine(color = limitLineColor, start = Offset(leftPad, yPx), end = Offset(leftPad + chartW, yPx), strokeWidth = 2f, pathEffect = dashEffect)
                    }
                    if (yLimitMax != null) {
                        val yPx = mapY(yLimitMax)
                        drawLine(color = limitLineColor, start = Offset(leftPad, yPx), end = Offset(leftPad + chartW, yPx), strokeWidth = 2f, pathEffect = dashEffect)
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            Text(
                "Pro zvolené období nejsou k dispozici žádná data.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        }
    }
}