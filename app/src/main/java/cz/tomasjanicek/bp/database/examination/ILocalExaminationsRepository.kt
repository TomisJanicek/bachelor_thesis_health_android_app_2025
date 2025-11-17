package cz.tomasjanicek.bp.database.examination

import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import kotlinx.coroutines.flow.Flow

interface ILocalExaminationsRepository {
    fun getAll(): Flow<List<Examination>>

    suspend fun insert(examination: Examination): Long

    suspend fun update(examination: Examination)

    suspend fun delete(examination: Examination)

    suspend fun getExamination(id: Long): Examination

    fun getAllWithDoctors(): Flow<List<ExaminationWithDoctor>>
}