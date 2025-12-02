package cz.tomasjanicek.bp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Singleton objekt pro pomocné funkce týkající se formátování data a času.
 * Použití 'object' místo 'class' s 'companion object' je pro utility bez stavu čistší.
 */
object DateUtils {

    // Formáty pro datum
    private const val CZECH_DATE_FORMAT = "d. M. yyyy" // Odebráno polstrování pro den a měsíc (dd -> d, MM -> M)
    private const val ENGLISH_DATE_FORMAT = "yyyy/MM/dd"

    // Nové formáty pro datum a čas
    private const val CZECH_DATETIME_FORMAT = "d. M. yyyy HH:mm"
    private const val ENGLISH_DATETIME_FORMAT = "yyyy/MM/dd HH:mm"

    private const val TIME_FORMAT = "HH:mm"

    /**
     * Vrátí naformátovaný řetězec obsahující POUZE datum.
     * @param date Timestamp v milisekundách.
     * @return Naformátované datum (např. "5. 1. 2025").
     */
    fun getDateString(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date

        val formatter = if (LanguageUtils.isLanguageCzech()) {
            // Pro češtinu je lepší Locale.forLanguageTag("cs-CZ"), ale Locale.GERMAN je častá náhrada, pokud to funguje
            SimpleDateFormat(CZECH_DATE_FORMAT, Locale.forLanguageTag("cs-CZ"))
        } else {
            SimpleDateFormat(ENGLISH_DATE_FORMAT, Locale.ENGLISH)
        }
        return formatter.format(calendar.time)
    }

    /**
     * Vrátí naformátovaný řetězec obsahující POUZE čas.
     * @param date Timestamp v milisekundách.
     * @return Naformátovaný čas (např. "14:30").
     */
    fun getTimeString(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date

        // Formát času je pro většinu lokalit stejný, není potřeba rozlišovat jazyk
        val formatter = SimpleDateFormat(TIME_FORMAT, Locale.forLanguageTag("cs-CZ"))
        return formatter.format(calendar.time)
    }

    /**
     * Vrátí naformátovaný řetězec obsahující DATUM i ČAS.
     * @param date Timestamp v milisekundách.
     * @return Naformátované datum a čas (např. "5. 1. 2025 14:30").
     */
    fun getDateTimeString(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date

        val formatter = if (LanguageUtils.isLanguageCzech()) {
            SimpleDateFormat(CZECH_DATETIME_FORMAT, Locale.forLanguageTag("cs-CZ"))
        } else {
            SimpleDateFormat(ENGLISH_DATETIME_FORMAT, Locale.ENGLISH)
        }
        return formatter.format(calendar.time)
    }
}