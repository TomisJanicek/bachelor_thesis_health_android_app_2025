package cz.tomasjanicek.bp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import cz.tomasjanicek.bp.services.ListConverter
import java.time.DayOfWeek

/**
 * Hlavní entita reprezentující lék a jeho nastavení.
 *
 * @param id Unikátní ID léku.
 * @param name Název léku (např. "Paralen").
 * @param note Poznámka pro uživatele (např. "Užívat po jídle").
 * @param dosage Dávka (např. 1.0, 5.0, 100.0).
 * @param unit Jednotka dávky.
 * @param isRegular Zda se jedná o pravidelné (true) nebo jednorázové (false) užívání.
 * @param regularityType Typ pravidelnosti (denní, týdenní). Důležité pro generování připomínek.
 * @param regularDays Dny v týdnu pro pravidelné užívání (relevantní pro [RegularityType.WEEKLY]).
 * @param regularTimes Časy během dne pro pravidelné užívání (v minutách od půlnoci, např. 8:00 = 480).
 * @param startDate Počáteční datum pro pravidelné užívání (v epoch millis).
 * @param singleDates Seznam konkrétních datumů a časů pro jednorázové užívání.
 */
@Entity(tableName = "medicines")
@TypeConverters(ListConverter::class)
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val note: String?,
    val dosage: Double,
    val unit: MedicineUnit,
    val isRegular: Boolean,
    val regularityType: RegularityType = RegularityType.DAILY, // Defaultní hodnota
    val regularDays: List<DayOfWeek>? = null,
    val regularTimes: List<Int>, // Časy v minutách od půlnoci (např. 8:00 -> 480)
    val startDate: Long? = null,
    val singleDates: List<Long>? = null,
    val endingType: EndingType = EndingType.INDEFINITELY,
    val endDate: Long? = null,
    val doseCount: Int? = null
)

/**
 * Entita reprezentující konkrétní záznam o užití léku (připomínku).
 * Toto bude hlavní zdroj dat pro seznam na hlavní obrazovce.
 *
 * @param id Unikátní ID záznamu.
 * @param medicineId ID léku, ke kterému se tento záznam vztahuje.
 * @param plannedDateTime Plánovaný čas užití (v epoch millis).
 * @param status Stav tohoto konkrétního užití.
 * @param completionDateTime Skutečný čas, kdy byl lék označen jako užitý.
 */
@Entity(tableName = "medicine_reminders")
data class MedicineReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long,
    val plannedDateTime: Long,
    var status: ReminderStatus = ReminderStatus.PLANNED,
    var completionDateTime: Long? = null
)

// --- Enumy pro lepší práci s daty ---

enum class MedicineUnit(val label: String) {
    TABLET("Tablety"),
    ML("ml"),
    DROP("Kapky"),
    MG("mg"),
    UNIT("Jednotky"),
    CUSTOM("Jiné")
}

enum class ReminderStatus {
    PLANNED, // Naplánováno
    COMPLETED, // Uživatel označil jako "hotovo"
    SKIPPED // Uživatel přeskočil (pro budoucí rozšíření)
}

enum class RegularityType {
    DAILY, // Každý den
    WEEKLY // Jen ve vybrané dny v týdnu
}

enum class EndingType(val label: String) {
    INDEFINITELY("Neomezeně"),
    UNTIL_DATE("Do data"),
    AFTER_DOSES("Počet dávek")
}