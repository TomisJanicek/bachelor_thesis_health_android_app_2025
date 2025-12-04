package cz.tomasjanicek.bp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "specialization") val specialization: String,
    @ColumnInfo(name = "name") val name: String? = null,
    @ColumnInfo(name = "phone") val phone: String? = null,
    @ColumnInfo(name = "email") val email: String? = null,
    @ColumnInfo(name = "image") val image: String? = null,
    @ColumnInfo(name = "description") var subtitle: String? = null,
    @ColumnInfo(name = "latitude") var latitude: Double? = null,
    @ColumnInfo(name = "longitude") var longitude: Double? = null,
    @ColumnInfo(name = "location") val addressLabel: String? = null, // např. "Třebíč Poliklinika" Využít reverse geolocation


)