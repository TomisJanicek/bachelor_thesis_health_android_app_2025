package cz.tomasjanicek.bp.database.examination

import cz.tomasjanicek.bp.model.Examination
import kotlinx.coroutines.flow.Flow

interface ILocalExaminationsRepository {
    fun getAll(): Flow<List<Examination>>

    suspend fun insert(examination: Examination): Long

    suspend fun update(examination: Examination)

    suspend fun delete(examination: Examination)

    suspend fun getExamination(id: Long): Examination
}