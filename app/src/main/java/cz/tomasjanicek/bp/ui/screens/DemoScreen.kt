package cz.tomasjanicek.bp.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import cz.tomasjanicek.bp.ui.elements.ChartPoint
import cz.tomasjanicek.bp.ui.elements.LineChartWithControlsDemo

@Composable
fun DemoScreen() {
    val now = System.currentTimeMillis()
    val dayMs = 86_400_000L
    val data = remember {
        (0..60).map { i ->
            val t = now - (60 - i) * dayMs
            val v = 120f + (Math.sin(i / 6.0) * 10 + Math.random() * 6).toFloat()
            ChartPoint(t, v)
        }
    }
    LineChartWithControlsDemo(allPoints = data)
}

@Preview(
    name = "Line Chart Demo",
    showBackground = true,
    backgroundColor = 0xFFFDFDFD
)
@Composable
fun PreviewLineChartDemo() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface {
            DemoScreen()
        }
    }
}