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

    // ... (painty zůstávají stejné) ...
    private val titlePaint = Paint().apply { /* ... */ }
    private val headerPaint = Paint().apply { /* ... */ }
    private val bodyPaint = Paint().apply { /* ... */ }
    private val tableHeaderPaint = Paint().apply { /* ... */ }


    fun exportStatsToPdf(
        chartData: List<StatsChartData>,
        periodType: StatsPeriodType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Uri? {
        Log.d(TAG, "[1] Spouštím `exportStatsToPdf`...")
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        // --- ZMĚNA: Začínáme s první stránkou ---
        var currentPage = document.startPage(pageInfo)
        var canvas = currentPage.canvas
        var yPos = margin + 20

        // 1. Hlavička dokumentu (jen na první stránce)
        canvas.drawText("Export statistik měření", margin, yPos, titlePaint)
        yPos += 30
        // ... (zbytek hlavičky)
        val dateRangeText = when (periodType) {
            StatsPeriodType.CUSTOM -> "Období: ${startDate.format(DateTimeFormatter.ofPattern("d.M.yy"))} - ${endDate.format(
                DateTimeFormatter.ofPattern("d.M.yy"))}"
            else -> "Období: ${periodType.label}"
        }
        canvas.drawText(dateRangeText, margin, yPos, bodyPaint)
        yPos += 20
        canvas.drawText("Exportováno dne: ${SimpleDateFormat("d. M. yyyy HH:mm",
            Locale.getDefault()).format(Date())}", margin, yPos, bodyPaint)
        yPos += 50


        // 2. Kreslení dat
        chartData.forEach { data ->
            // --- KONTROLA MÍSTA PRO CELOU KATEGORII ---
            // Odhad místa: 50px pro nadpis + (320px na parametr) * počet parametrů
            val estimatedHeightForCategory = 50 + (data.categoryWithFields.fields.size * 320)
            if (yPos + estimatedHeightForCategory > pageHeight - margin) {
                // --- ZMĚNA: Správné ukončení staré stránky a vytvoření nové ---
                document.finishPage(currentPage)
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas // <- KLÍČOVÉ: Získáme nový canvas pro novou stránku!
                yPos = margin
                drawPageHeader(canvas) // Nakreslíme hlavičku "pokračování"
                yPos += 40
            }

            // Název kategorie
            canvas.drawText(data.categoryWithFields.category.name, margin, yPos, headerPaint)
            yPos += 30

            // Grafy a tabulky pro jednotlivé parametry
            data.categoryWithFields.fields.forEach { field ->
                val entries = data.measurementsWithValues.mapNotNull { m ->
                    m.values.find { v -> v.categoryFieldId == field.id }?.let { value ->
                        Entry(m.measurement.measuredAt.toFloat(), value.value.toFloat())
                    }
                }.sortedBy { it.x }

                if (entries.isNotEmpty()) {
                    // --- KONTROLA MÍSTA PRO JEDEN PARAMETR (GRAF + TABULKA) ---
                    val estimatedHeightForField = 320f // Pevný odhad pro graf a pár řádků tabulky
                    if (yPos + estimatedHeightForField > pageHeight - margin) {
                        // --- ZMĚNA: Správné ukončení staré stránky a vytvoření nové ---
                        document.finishPage(currentPage)
                        currentPage = document.startPage(pageInfo)
                        canvas = currentPage.canvas // <- KLÍČOVÉ: Získáme nový canvas!
                        yPos = margin
                        drawPageHeader(canvas)
                        yPos += 40
                    }

                    drawChart(canvas, entries, field.label, yPos)
                    yPos += 280

                    yPos = drawDataTable(canvas, entries, field, yPos)
                    yPos += 40
                }
            }
        }

        // --- ZMĚNA: Ukončení poslední aktivní stránky ---
        document.finishPage(currentPage)
        Log.d(TAG, "[2] Kreslení do PDF dokumentu dokončeno.")

        // 3. Uložení souboru (tato část je už správně)
        return try {
            val file = File(context.cacheDir, "export_mereni_${System.currentTimeMillis()}.pdf")
            Log.d(TAG, "[3] Vytvářím soubor v cestě: ${file.absolutePath}")

            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()
            Log.d(TAG, "[4] Soubor PDF úspěšně zapsán na disk.")

            val authority = "${context.packageName}.provider"
            Log.d(TAG, "[5] Získávám URI s autoritou: '$authority'")
            FileProvider.getUriForFile(context, authority, file)

        } catch (e: Exception) {
            Log.e(TAG, "[CHYBA] Selhání při ukládání souboru nebo získávání URI!", e)
            e.printStackTrace()
            null
        }
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
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.valueFormatter = object : ValueFormatter() {
                private val formatter = SimpleDateFormat("d.M", Locale.getDefault())
                override fun getFormattedValue(value: Float): String {
                    return formatter.format(Date(value.toLong()))
                }
            }
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = Color.LTGRAY
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

    private fun drawDataTable(canvas: Canvas, entries: List<Entry>, field: cz.tomasjanicek.bp.model.MeasurementCategoryField, yPos: Float): Float {
        var currentY = yPos
        val col1X = margin
        val col2X = margin + 250
        canvas.drawText("Datum", col1X, currentY, tableHeaderPaint)
        canvas.drawText("Hodnota (${field.unit ?: ""})", col2X, currentY, tableHeaderPaint)
        currentY += 25
        entries.forEach { entry ->
            val dateStr = SimpleDateFormat("d. M. yyyy HH:mm", Locale.getDefault()).format(Date(entry.x.toLong()))
            val valueStr = String.format(Locale.US, "%.2f", entry.y)
            canvas.drawText(dateStr, col1X, currentY, bodyPaint)
            canvas.drawText(valueStr, col2X, currentY, bodyPaint)
            currentY += 20
        }
        return currentY
    }
}