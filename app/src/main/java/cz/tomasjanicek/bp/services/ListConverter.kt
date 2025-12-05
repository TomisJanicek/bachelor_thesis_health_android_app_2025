package cz.tomasjanicek.bp.services

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek

class ListConverter {
    private val gson = Gson()

    // --- Pro List<DayOfWeek> ---
    @TypeConverter
    fun fromDayOfWeekList(days: List<DayOfWeek>?): String? {
        return days?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDayOfWeekList(json: String?): List<DayOfWeek>? {
        return json?.let {
            val type = object : TypeToken<List<DayOfWeek>>() {}.type
            gson.fromJson(it, type)
        }
    }

    // --- Pro List<Int> ---
    @TypeConverter
    fun fromIntList(times: List<Int>?): String? {
        return times?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toIntList(json: String?): List<Int>? {
        return json?.let {
            val type = object : TypeToken<List<Int>>() {}.type
            gson.fromJson(it, type)
        }
    }

    // --- Pro List<Long> ---
    @TypeConverter
    fun fromLongList(dates: List<Long>?): String? {
        return dates?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toLongList(json: String?): List<Long>? {
        return json?.let {
            val type = object : TypeToken<List<Long>>() {}.type
            gson.fromJson(it, type)
        }
    }
}