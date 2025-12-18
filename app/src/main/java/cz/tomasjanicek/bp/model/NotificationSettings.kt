package cz.tomasjanicek.bp.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

// b) Kdy chce připomenout léky
enum class MedicineNotificationTime(val label: String, val minutesOffset: Long) {
    AT_TIME("V čas užití", 0),
    BEFORE_15_MIN("15 minut předem", 15),
    BEFORE_30_MIN("30 minut předem", 30),
    BEFORE_1_HOUR("1 hodinu předem", 60)
}

// c) Kdy chce připomenout prohlídku
enum class ExaminationNotificationTime(val label: String) {
    AT_TIME("V čas prohlídky"),
    BEFORE_1_HOUR("1 hodinu předem"),
    BEFORE_2_HOURS("2 hodiny předem"),
    DAY_BEFORE_9_AM("Den předem v 9:00"),
    DAY_BEFORE_6_PM("Den předem v 18:00"),
    WEEK_BEFORE("Týden předem");

    /**
     * Vypočítá přesný čas notifikace na základě času prohlídky.
     */
    fun calculateTriggerTime(examTimestamp: Long): Long {
        val examDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(examTimestamp),
            ZoneId.systemDefault()
        )

        val triggerDateTime = when (this) {
            AT_TIME -> examDateTime
            BEFORE_1_HOUR -> examDateTime.minusHours(1)
            BEFORE_2_HOURS -> examDateTime.minusHours(2)
            DAY_BEFORE_9_AM -> examDateTime.minusDays(1).withHour(9).withMinute(0).withSecond(0)
            DAY_BEFORE_6_PM -> examDateTime.minusDays(1).withHour(18).withMinute(0).withSecond(0)
            WEEK_BEFORE -> examDateTime.minusWeeks(1)
        }

        return triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}