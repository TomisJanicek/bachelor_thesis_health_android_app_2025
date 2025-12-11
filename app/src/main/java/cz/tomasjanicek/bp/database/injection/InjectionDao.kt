package cz.tomasjanicek.bp.database.injection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cz.tomasjanicek.bp.model.Injection
import kotlinx.coroutines.flow.Flow

@Dao
interface InjectionDao {
    @Query("SELECT * FROM injections ORDER BY date DESC")
    fun getAllInjections(): Flow<List<Injection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInjection(injection: Injection)

    @Query("DELETE FROM injections WHERE id = :id")
    suspend fun deleteInjectionById(id: Long)

    @Query("SELECT * FROM injections WHERE id = :id")
    fun getInjectionById(id: Long): Flow<Injection>

    @Query("SELECT * FROM injections")
    suspend fun getAllList(): List<Injection>

    @Query("DELETE FROM injections")
    suspend fun deleteAll()
}