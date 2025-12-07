package cz.tomasjanicek.bp.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "injections")
data class Injection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Název vakcíny, např. "Hexavakcína"
    val disease: String, // Proti čemu chrání, např. "Záškrt, tetanus, černý kašel..."
    val category: InjectionCategory,
    val date: Long, // Datum očkování
    val note: String? = null // Poznámka
)