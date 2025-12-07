package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.cycle.CycleRecordDao
import cz.tomasjanicek.bp.database.cycle.CycleRepositoryImpl
import cz.tomasjanicek.bp.database.cycle.ICycleRepository
import cz.tomasjanicek.bp.database.doctor.DoctorDao
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.database.doctor.LocalDoctorsRepositoryImpl
import cz.tomasjanicek.bp.database.examination.ExaminationDao
import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.database.examination.LocalExaminationsRepositoryImpl
import cz.tomasjanicek.bp.database.injection.IInjectionRepository
import cz.tomasjanicek.bp.database.injection.InjectionDao
import cz.tomasjanicek.bp.database.injection.InjectionRepositoryImpl
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementsRepository
import cz.tomasjanicek.bp.database.measurement.LocalMeasurementCategoriesRepositoryImpl
import cz.tomasjanicek.bp.database.measurement.LocalMeasurementsRepositoryImpl
import cz.tomasjanicek.bp.database.measurement.MeasurementCategoryDao
import cz.tomasjanicek.bp.database.measurement.MeasurementDao
import cz.tomasjanicek.bp.database.medicine.IMedicineRepository
import cz.tomasjanicek.bp.database.medicine.LocalMedicineRepositoryImpl
import cz.tomasjanicek.bp.database.medicine.MedicineDao
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

    @Provides
    @Singleton
    fun provideMedicineRepository(
        dao: MedicineDao
    ): IMedicineRepository = LocalMedicineRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideCycleRepository(
        dao: CycleRecordDao
    ): ICycleRepository = CycleRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideInjectionRepository(
        dao: InjectionDao
    ): IInjectionRepository = InjectionRepositoryImpl(dao)

}