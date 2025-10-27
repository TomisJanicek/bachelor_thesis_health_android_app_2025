package cz.tomasjanicek.bp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "specialization") val specialization: String,
    @ColumnInfo(name = "image") val image: String,
    @ColumnInfo(name = "description") var subtitle: String,
    @ColumnInfo(name = "latitude") var latitude: Double?,
    @ColumnInfo(name = "longitude") var longitude: Double?,
    @ColumnInfo(name = "location") val location: String? = null, // např. "Třebíč Poliklinika"


)