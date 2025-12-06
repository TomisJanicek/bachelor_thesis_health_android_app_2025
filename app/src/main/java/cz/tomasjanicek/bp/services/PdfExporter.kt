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
    private val lineHeight = 20f // Výška jednoho řádku v tabulce

    // ... definice Paint objektů (zůstávají stejné) ...
    private val titlePaint = Paint().apply { color = Color.BLACK; textSize = 24f; isFakeBoldText = true }
    private val headerPaint = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
    private val bodyPaint = Paint().apply { color = Color.DKGRAY; textSize = 14f }
    private val tableHeaderPaint = Paint().apply { color = Color.BLACK; textSize = 14f; isFakeBoldText = true }


    fun exportStatsToPdf(
        chartData: List<StatsChartData>,
        periodType: StatsPeriodType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Uri? {
        Log.d(TAG, "[1] Spouštím `exportStatsToPdf`...")
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        // --- ZDE JE KLÍČOVÁ OPRAVA ---
        // 1. Vytvoříme stránku POUZE JEDNOU
        val firstPage = document.startPage(pageInfo)

        // 2. Vytvoříme PageState s touto jednou stránkou a jejím canvasem
        var pageState = PageState(
            currentPage = firstPage,
            canvas = firstPage.canvas,
            yPos = margin + 20
        )

        // Hlavička dokumentu
        pageState.canvas.drawText("Export statistik měření", margin, pageState.yPos, titlePaint)
        pageState.yPos += 30
        // ... (zbytek hlavičky)
        val dateRangeText = when (periodType) {
            StatsPeriodType.CUSTOM -> "Období: ${startDate.format(DateTimeFormatter.ofPattern("d.M.yy"))} - ${endDate.format(DateTimeFormatter.ofPattern("d.M.yy"))}"
            else -> "Období: ${periodType.label}"
        }
        pageState.canvas.drawText(dateRangeText, margin, pageState.yPos, bodyPaint)
        pageState.yPos += 20
        pageState.canvas.drawText("Exportováno dne: ${SimpleDateFormat("d. M. yyyy HH:mm", Locale.getDefault()).format(Date())}", margin, pageState.yPos, bodyPaint)
        pageState.yPos += 50

        // Kreslení dat
        chartData.forEach { data ->
            val estimatedHeightForCategory = 50 + (data.categoryWithFields.fields.size * 320)
            if (pageState.yPos + estimatedHeightForCategory > pageHeight - margin) {
                pageState = createNewPage(document, pageState, pageInfo)
            }

            pageState.canvas.drawText(data.categoryWithFields.category.name, margin, pageState.yPos, headerPaint)
            pageState.yPos += 30

            data.categoryWithFields.fields.forEach { field ->
                val entries = data.measurementsWithValues.mapNotNull { m ->
                    m.values.find { v -> v.categoryFieldId == field.id }?.let { value ->
                        Entry(m.measurement.measuredAt.toFloat(), value.value.toFloat())
                    }
                }.sortedBy { it.x }

                if (entries.isNotEmpty()) {
                    val estimatedHeightForField = 320f
                    if (pageState.yPos + estimatedHeightForField > pageHeight - margin) {
                        pageState = createNewPage(document, pageState, pageInfo)
                    }

                    drawChart(pageState.canvas, entries, field.label, pageState.yPos)
                    pageState.yPos += 280

                    // --- ZMĚNA: Předáváme celý stav a dokument ---
                    pageState = drawDataTable(document, pageInfo, pageState, entries, field)
                    pageState.yPos += 40
                }
            }
        }

        document.finishPage(pageState.currentPage)
        Log.d(TAG, "[2] Kreslení do PDF dokumentu dokončeno.")

        // Uložení souboru (zůstává stejné)
        return try {
            val file = File(context.cacheDir, "export_mereni_${System.currentTimeMillis()}.pdf")
            // ...
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()
            // ...
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            Log.e(TAG, "[CHYBA] Selhání při ukládání souboru nebo získávání URI!", e)
            null
        }
    }

    // --- NOVÁ POMOCNÁ FUNKCE pro vytvoření nové stránky ---
    private fun createNewPage(document: PdfDocument, oldPageState: PageState, pageInfo: PdfDocument.PageInfo): PageState {
        document.finishPage(oldPageState.currentPage)
        val newPage = document.startPage(pageInfo)
        val newCanvas = newPage.canvas
        var newYPos = margin
        drawPageHeader(newCanvas)
        newYPos += 40
        return PageState(newPage, newCanvas, newYPos)
    }

    private fun drawPageHeader(canvas: Canvas) {
        val headerText = "Pokračování exportu..."
        bodyPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText(headerText, margin, margin, bodyPaint)
        bodyPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

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

        // Správné pořadí: save -> translate -> draw -> restore
        canvas.save()
        canvas.translate(margin, yPos)
        chart.draw(canvas) // <-- Kreslení musí být ZDE
        canvas.restore()
    }

    // --- ZCELA PŘEPRACOVANÁ FUNKCE `drawDataTable` ---
    private fun drawDataTable(
        document: PdfDocument,
        pageInfo: PdfDocument.PageInfo,
        initialState: PageState,
        entries: List<Entry>,
        field: cz.tomasjanicek.bp.model.MeasurementCategoryField
    ): PageState {
        var currentState = initialState.copy()
        val col1X = margin
        val col2X = margin + 250

        // Kontrola místa pro hlavičku
        if (currentState.yPos + lineHeight > pageHeight - margin) {
            currentState = createNewPage(document, currentState, pageInfo)
        }
        currentState.canvas.drawText("Datum", col1X, currentState.yPos, tableHeaderPaint)
        currentState.canvas.drawText("Hodnota (${field.unit ?: ""})", col2X, currentState.yPos, tableHeaderPaint)
        currentState.yPos += 25

        // Kreslení řádků
        entries.forEach { entry ->
            // Kontrola místa pro další řádek
            if (currentState.yPos + lineHeight > pageHeight - margin) {
                currentState = createNewPage(document, currentState, pageInfo)
            }
            val dateStr = SimpleDateFormat("d. M. yyyy HH:mm", Locale.getDefault()).format(Date(entry.x.toLong()))
            val valueStr = String.format(Locale.US, "%.2f", entry.y)
            currentState.canvas.drawText(dateStr, col1X, currentState.yPos, bodyPaint)
            currentState.canvas.drawText(valueStr, col2X, currentState.yPos, bodyPaint)
            currentState.yPos += lineHeight
        }
        return currentState
    }
}

private data class PageState(
    var currentPage: PdfDocument.Page,
    var canvas: Canvas,
    var yPos: Float
)