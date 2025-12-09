package cz.tomasjanicek.bp.services

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface // <-- PŘIDANÝ IMPORT
import android.graphics.pdf.PdfDocument // <-- ZMĚNA: Správný import pro PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.ui.screens.stats.StatsChartData
import cz.tomasjanicek.bp.ui.screens.stats.StatsPeriodType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class PdfExporter(private val context: Context) {

    private val TAG = "PDF_EXPORT_LOG"
    private val pageHeight = 1120
    private val pageWidth = 792
    private val margin = 40f
    private val contentWidth = pageWidth - 2 * margin

    private val titlePaint = Paint().apply { color = Color.BLACK; textSize = 24f; isFakeBoldText = true }
    private val headerPaint = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
    private val bodyPaint = Paint().apply { color = Color.DKGRAY; textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) }
    private val tableHeaderPaint = Paint().apply { color = Color.BLACK; textSize = 14f; isFakeBoldText = true }
    private val italicPaint = Paint(bodyPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }
    fun exportStatsToPdf(
        chartData: List<StatsChartData>,
        periodType: StatsPeriodType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Uri? {
        Log.d(TAG, "[1] Spouštím `exportStatsToPdf`...")
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        // Začneme s první stránkou
        var pageState = startNewPage(document, null, pageInfo)

        // --- Kreslení obsahu ---

        // Hlavička dokumentu na první stránce
        pageState.canvas.drawText("Export statistik měření", margin, pageState.yPos, titlePaint)
        pageState.yPos += 30
        val dateRangeText = when (periodType) {
            StatsPeriodType.CUSTOM -> "Období: ${startDate.format(DateTimeFormatter.ofPattern("d.M.yy"))} - ${endDate.format(DateTimeFormatter.ofPattern("d.M.yy"))}"
            else -> "Období: ${periodType.label}"
        }
        pageState.canvas.drawText(dateRangeText, margin, pageState.yPos, bodyPaint)
        pageState.yPos += 20
        pageState.canvas.drawText("Exportováno dne: ${SimpleDateFormat("d. M. yyyy HH:mm", Locale.getDefault()).format(Date())}", margin, pageState.yPos, bodyPaint)
        pageState.yPos += 50

        // Kreslení jednotlivých kategorií a grafů
        chartData.forEach { data ->
            // Potřebujeme místo na nadpis kategorie
            pageState = ensureSpace(document, pageState, pageInfo, 50f)
            pageState.canvas.drawText(data.categoryWithFields.category.name, margin, pageState.yPos, headerPaint)
            pageState.yPos += 30

            data.categoryWithFields.fields.forEach { field ->
                val entries = data.measurementsWithValues.mapNotNull { m ->
                    m.values.find { v -> v.categoryFieldId == field.id }?.let { value ->
                        Entry(m.measurement.measuredAt.toFloat(), value.value.toFloat())
                    }
                }.sortedBy { it.x }

                if (entries.isNotEmpty()) {
                    // Potřebujeme místo pro graf
                    pageState = ensureSpace(document, pageState, pageInfo, 280f)
                    drawChart(pageState.canvas, entries, field.label, pageState.yPos)
                    pageState.yPos += 280

                    // Tabulka se o stránkování postará sama
                    pageState = drawDataTable(document, pageInfo, pageState, entries, field)
                    pageState.yPos += 40 // Mezera po tabulce
                }
            }
        }

        // Dokončíme poslední otevřenou stránku
        document.finishPage(pageState.currentPage)
        Log.d(TAG, "[2] Kreslení do PDF dokumentu dokončeno.")

        // Uložení souboru
        return try {
            val file = File(context.cacheDir, "export_mereni_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()
            Log.d(TAG, "[3] PDF soubor úspěšně uložen do: ${file.absolutePath}")
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            Log.e(TAG, "[CHYBA] Selhání při ukládání souboru nebo získávání URI!", e)
            null
        }
    }

    /**
     * OPRAVENO: Tato funkce je teď jediná, která vytváří novou stránku.
     * Ukončí starou stránku (pokud existuje) a začne novou.
     */
    private fun startNewPage(document: PdfDocument, oldPage: PdfDocument.Page?, pageInfo: PdfDocument.PageInfo): PageState {
        oldPage?.let { document.finishPage(it) }
        val newPage = document.startPage(pageInfo)
        var yPos = margin
        // Vykreslíme záhlaví jen na stránkách > 1
        if (oldPage != null) {
            newPage.canvas.drawText("Pokračování exportu...", margin, yPos, italicPaint)
            yPos += 40
        }
        return PageState(newPage, newPage.canvas, yPos)
    }

    /**
     * OPRAVENO: Tato funkce zkontroluje, zda se na stránku vejde obsah dané výšky.
     * Pokud ne, vytvoří novou stránku.
     */
    private fun ensureSpace(document: PdfDocument, currentState: PageState, pageInfo: PdfDocument.PageInfo, heightNeeded: Float): PageState {
        return if (currentState.yPos + heightNeeded > pageHeight - margin) {
            startNewPage(document, currentState.currentPage, pageInfo)
        } else {
            currentState
        }
    }

    /**
     * ZŮSTÁVÁ STEJNÉ: Kreslí graf.
     */
    private fun drawChart(canvas: Canvas, entries: List<Entry>, label: String, yPos: Float) {
        val chart = LineChart(context).apply {
            layout(0, 0, contentWidth.toInt(), 250)
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            isDragEnabled = false
            setScaleEnabled(false)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    private val formatter = SimpleDateFormat("d.M", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String = formatter.format(Date(value.toLong()))
                }
            }
            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
            }
            val dataSet = LineDataSet(entries, label).apply {
                color = Color.parseColor("#E91E63") // MyPink
                setCircleColor(color)
                circleRadius = 4f
                setDrawValues(false)
                lineWidth = 2f
            }
            this.data = LineData(dataSet)
        }

        canvas.save()
        canvas.translate(margin, yPos)
        chart.draw(canvas)
        canvas.restore()
    }

    /**
     * OPRAVENO: Funkce nyní používá `ensureSpace` pro kontrolu místa pro každý řádek.
     */
    private fun drawDataTable(
        document: PdfDocument,
        pageInfo: PdfDocument.PageInfo,
        initialState: PageState,
        entries: List<Entry>,
        field: MeasurementCategoryField
    ): PageState {
        var currentState = initialState

        // Místo pro hlavičku tabulky
        val lineHeightHeader = 25f
        currentState = ensureSpace(document, currentState, pageInfo, lineHeightHeader)
        val col1X = margin
        val col2X = margin + 250
        currentState.canvas.drawText("Datum", col1X, currentState.yPos, tableHeaderPaint)
        currentState.canvas.drawText("Hodnota (${field.unit ?: ""})", col2X, currentState.yPos, tableHeaderPaint)
        currentState.yPos += lineHeightHeader

        // Kreslení řádků
        val lineHeightRow = 20f
        entries.forEach { entry ->
            // Místo pro řádek tabulky
            currentState = ensureSpace(document, currentState, pageInfo, lineHeightRow)
            val dateStr = SimpleDateFormat("d. M. yyyy HH:mm", Locale.getDefault()).format(Date(entry.x.toLong()))
            val valueStr = String.format(Locale.US, "%.2f", entry.y)
            currentState.canvas.drawText(dateStr, col1X, currentState.yPos, bodyPaint)
            currentState.canvas.drawText(valueStr, col2X, currentState.yPos, bodyPaint)
            currentState.yPos += lineHeightRow
        }
        return currentState
    }
}

private data class PageState(
    var currentPage: PdfDocument.Page,
    var canvas: Canvas,
    var yPos: Float
)