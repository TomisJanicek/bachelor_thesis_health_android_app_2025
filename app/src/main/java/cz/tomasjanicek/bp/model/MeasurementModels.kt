package cz.tomasjanicek.bp.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Kategorie měření – uživatelsky definovatelné skupiny (BMI, Váha, Krevní tlak, Krevní test, ...).
 */
@Entity(
    tableName = "measurement_categories"
)
data class MeasurementCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,             // Zobrazované jméno kategorie

    @ColumnInfo(name = "description")
    val description: String? = null
)

/**
 * Definice jednotlivých "polí" v kategorii (např. systolic/diastolic, BMI, váha, glucose, ...).
 */
@Entity(
    tableName = "measurement_category_fields",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class MeasurementCategoryField(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val categoryId: Long,

    @ColumnInfo(name = "name")
    val name: String,             // Např. "systolic", "diastolic", "BMI", "weight", "glucose"

    @ColumnInfo(name = "label")
    val label: String,            // Uživatelsky přívětivý název (např. "Systolický tlak")

    @ColumnInfo(name = "unit")
    val unit: String? = null,      // Např. "kg", "mmHg", "mmol/L"

    @ColumnInfo(name = "min_value")
    val minValue: Double? = null, // Volitelné minimum

    @ColumnInfo(name = "max_value")
    val maxValue: Double? = null  // Volitelné maximum
)

/**
 * Jedno konkrétní měření v čase (např. krevní tlak dne 1.1.2025 v 10:00).
 */
@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val categoryId: Long,

    @ColumnInfo(name = "measuredAt")
    val measuredAt: Long          // timestamp v ms
)

/**
 * Konkrétní hodnota v rámci jednoho měření (např. systolic = 120, diastolic = 80).
 */
@Entity(
    tableName = "measurement_values",
    foreignKeys = [
        ForeignKey(
            entity = Measurement::class,
            parentColumns = ["id"],
            childColumns = ["measurementId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MeasurementCategoryField::class,
            parentColumns = ["id"],
            childColumns = ["categoryFieldId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("measurementId"),
        Index("categoryFieldId")
    ]
)
data class MeasurementValue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val measurementId: Long,
    val categoryFieldId: Long,

    @ColumnInfo(name = "value")
    val value: Double
)

/**
 * Relační objekt: Kategorie + její definovaná pole.
 */
data class MeasurementCategoryWithFields(
    @Embedded
    val category: MeasurementCategory,

    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val fields: List<MeasurementCategoryField>
)

/**
 * Relační objekt: Měření + jeho hodnoty + kategorie.
 */
data class MeasurementWithValues(
    @Embedded
    val measurement: Measurement,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: MeasurementCategory,

    @Relation(
        parentColumn = "id",
        entityColumn = "measurementId"
    )
    val values: List<MeasurementValue>
)