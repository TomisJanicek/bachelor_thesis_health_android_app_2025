package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.doctor.DoctorDao
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.database.doctor.LocalDoctorsRepositoryImpl
import cz.tomasjanicek.bp.database.examination.ExaminationDao
import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.database.examination.LocalExaminationsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideExaminationsRepository(
        dao: ExaminationDao
    ): ILocalExaminationsRepository = LocalExaminationsRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideDoctorsRepository(
        dao: DoctorDao
    ): ILocalDoctorsRepository = LocalDoctorsRepositoryImpl(dao)
}