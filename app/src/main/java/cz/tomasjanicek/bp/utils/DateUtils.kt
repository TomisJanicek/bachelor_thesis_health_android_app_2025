package cz.tomasjanicek.bp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateUtils {

    companion object {

        private val CZECH_FORMAT = "dd. MM. yyyy"
        private val ENGLISH_FORMAT = "yyyy/MM/dd"

        fun getDateString(date: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date

            val formater: SimpleDateFormat
            if (LanguageUtils.isLanguageCzech()){
                formater = SimpleDateFormat(CZECH_FORMAT, Locale.GERMAN)
            } else {
                formater = SimpleDateFormat(ENGLISH_FORMAT, Locale.ENGLISH)
            }
            return formater.format(calendar.time)
        }


    }

}