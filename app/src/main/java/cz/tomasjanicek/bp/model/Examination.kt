package cz.tomasjanicek.bp.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "examinations",
    foreignKeys = [
        ForeignKey(
            entity = Doctor::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("doctorId")]
)
data class Examination(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val doctorId: Long,
    val type: ExaminationType, // ENUM níže
    val purpose: String,       // např. "Jdu preventivně", "Bude mi trhat zub"
    val note: String? = null,  // poznámka z formuláře
    val result: String? = null,// výsledek (dialog "Doplň informace")
    val dateTime: Long,        // timestamp
    val status: ExaminationStatus = ExaminationStatus.PLANNED // viz níže
)

enum class ExaminationType {
    PROHLIDKA, ZAKROK, VYSETRENI, ODBER_KRVE
}

enum class ExaminationStatus {
    PLANNED, COMPLETED, CANCELLED
}