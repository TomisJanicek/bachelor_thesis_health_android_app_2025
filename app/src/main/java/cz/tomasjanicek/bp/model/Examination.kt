package cz.tomasjanicek.bp.model

import androidx.compose.ui.graphics.Color
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import cz.tomasjanicek.bp.ui.theme.TagGreen
import cz.tomasjanicek.bp.ui.theme.TagGrey
import cz.tomasjanicek.bp.ui.theme.TagOrange
import cz.tomasjanicek.bp.ui.theme.TagYellow
import cz.tomasjanicek.bp.ui.theme.TagPurple

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
    @PrimaryKey(autoGenerate = true) val id: Long? = 0,
    val doctorId: Long?,
    val type: ExaminationType, // ENUM níže
    val purpose: String,       // např. "Jdu preventivně", "Bude mi trhat zub"
    val note: String? = null,  // poznámka z formuláře
    val result: String? = null,// výsledek (dialog "Doplň informace")
    val dateTime: Long,        // timestamp
    val status: ExaminationStatus = ExaminationStatus.PLANNED // viz níže
)
enum class ExaminationType(val label: String, val tagColor: Color) {
    PROHLIDKA("Prohlídka", TagYellow),
    ZAKROK("Zákrok", TagGreen),
    VYSETRENI("Vyšetření", TagOrange),
    ODBER_KRVE("Odběr krve", TagPurple),

    JINE("Jiné", TagGrey)
}

enum class ExaminationStatus {
    PLANNED, COMPLETED, CANCELLED, OVERDUE
}

data class ExaminationWithDoctor(
    @Embedded
    val examination: Examination,

    @Relation(
        parentColumn = "doctorId", // Sloupec v 'examinations' tabulce
        entityColumn = "id"        // Sloupec v 'doctors' tabulce
    )
    val doctor: Doctor?
)