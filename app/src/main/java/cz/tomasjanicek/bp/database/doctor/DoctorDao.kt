package cz.tomasjanicek.bp.database.doctor

import androidx.room.*
import cz.tomasjanicek.bp.model.Doctor
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {

    @Query("SELECT * FROM doctors ORDER BY id ASC")
    fun getAll(): Flow<List<Doctor>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(doctor: Doctor): Long

    @Update
    suspend fun update(doctor: Doctor)

    @Delete
    suspend fun delete(doctor: Doctor)

    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctor(id: Long?): Doctor?

    // Kompletní smazání (použijeme jen výjimečně, např. při odinstalaci nebo tvrdém resetu)
    @Query("DELETE FROM doctors")
    suspend fun deleteAll()

    // Inicializace seznamu - použijeme OnConflictStrategy.IGNORE, aby se nepřepsala existující data, pokud by se ID shodovala
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(doctors: List<Doctor>)

    @Query("SELECT COUNT(*) FROM doctors")
    suspend fun getCount(): Int

    @Transaction
    @Query("SELECT * FROM doctors WHERE id = :doctorId")
    fun getDoctorWithData(doctorId: Long): Flow<Doctor?>

    @Query("SELECT * FROM doctors")
    suspend fun getAllList(): List<Doctor>

    // --- NOVÁ METODA PRO RESET (Soft Delete) ---
    // Smaže pouze uživatelská data, ale zachová strukturu tabulky (ID, Specializaci, Obrázek)
    // Pozor: Sloupec v DB se jmenuje 'description', i když v modelu je to 'subtitle'
    @Query("""
        UPDATE doctors 
        SET name = NULL, 
            phone = NULL, 
            email = NULL, 
            description = NULL, 
            latitude = NULL, 
            longitude = NULL, 
            location = NULL
    """)
    suspend fun resetAllDoctorData()

}