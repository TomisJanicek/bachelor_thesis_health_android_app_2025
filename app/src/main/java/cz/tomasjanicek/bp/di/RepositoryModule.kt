package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.doctor.DoctorDao
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.database.doctor.LocalDoctorsRepositoryImpl
import cz.tomasjanicek.bp.database.examination.ExaminationDao
import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.database.examination.LocalExaminationsRepositoryImpl
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementsRepository
import cz.tomasjanicek.bp.database.measurement.LocalMeasurementCategoriesRepositoryImpl
import cz.tomasjanicek.bp.database.measurement.LocalMeasurementsRepositoryImpl
import cz.tomasjanicek.bp.database.measurement.MeasurementCategoryDao
import cz.tomasjanicek.bp.database.measurement.MeasurementDao
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

    @Provides
    @Singleton
    fun provideMeasurementCategoriesRepository(
        dao: MeasurementCategoryDao
    ): ILocalMeasurementCategoriesRepository =
        LocalMeasurementCategoriesRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideMeasurementsRepository(
        dao: MeasurementDao
    ): ILocalMeasurementsRepository =
        LocalMeasurementsRepositoryImpl(dao)
}