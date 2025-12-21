package cz.tomasjanicek.bp.services

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.text.format

/**
* Třída s Type Convertery pro Room.
* Říká databázi, jak ukládat a načítat typy, kterým nativně nerozumí.
*/
class Converters {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate { // Změna: Návratový typ už není LocalDate? (s otazníkem), ale LocalDate (bez)
        if (value.isNullOrBlank() || value == "0000-00-00") {
            // ZMĚNA: Místo null vrátíme "záchranné datum".
            // Tím zabráníme pádu aplikace, protože CycleRecord vyžaduje, aby datum nebylo null.
            return LocalDate.of(1970, 1, 1)
        }

        return try {
            LocalDate.parse(value, dateFormatter)
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            // I v případě chyby parsování vrátíme záchranné datum
            LocalDate.of(1970, 1, 1)
        }
    }
}