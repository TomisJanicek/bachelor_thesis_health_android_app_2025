package cz.tomasjanicek.bp.services

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.text.format

/**
* Třída s Type Convertery pro Room.
* Říká databázi, jak ukládat a načítat typy, kterým nativně nerozumí.
*/
class Converters {

    // Definuje formát, ve kterém budeme datum ukládat jako text (ISO standard)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Převede LocalDate na String, aby ho Room mohl uložit.
     * @param date Objekt LocalDate nebo null.
     * @return Textová reprezentace data (např. "2025-12-06") nebo null.
     */
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    /**
     * Převede String z databáze zpět na LocalDate.
     * @param value Textová reprezentace data (např. "2025-12-06") nebo null.
     * @return Objekt LocalDate nebo null.
     */
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let {
            return LocalDate.parse(it, dateFormatter)
        }
    }
}