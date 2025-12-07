package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.AppDatabase
import cz.tomasjanicek.bp.database.cycle.CycleRecordDao
import cz.tomasjanicek.bp.database.cycle.CycleRepositoryImpl
import cz.tomasjanicek.bp.database.cycle.ICycleRepository
import cz.tomasjanicek.bp.database.doctor.DoctorDao
import cz.tomasjanicek.bp.database.examination.ExaminationDao
import cz.tomasjanicek.bp.database.injection.IInjectionRepository
import cz.tomasjanicek.bp.database.injection.InjectionDao
import cz.tomasjanicek.bp.database.injection.InjectionRepositoryImpl
import cz.tomasjanicek.bp.database.measurement.MeasurementCategoryDao
import cz.tomasjanicek.bp.database.measurement.MeasurementDao
import cz.tomasjanicek.bp.database.medicine.MedicineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    @Singleton
    fun provideDoctorDao(db: AppDatabase): DoctorDao = db.doctorDao()

    @Provides
    @Singleton
    fun provideExaminationDao(db: AppDatabase): ExaminationDao = db.examinationDao()

    @Provides
    @Singleton
    fun provideMeasurementCategoryDao(db: AppDatabase): MeasurementCategoryDao =
        db.measurementCategoryDao()

    @Provides
    @Singleton
    fun provideMeasurementDao(db: AppDatabase): MeasurementDao =
        db.measurementDao()

    @Provides
    @Singleton
    fun provideMedicineDao(db: AppDatabase): MedicineDao = db.medicineDao()

    @Provides
    @Singleton
    fun provideCycleRecordDao(db: AppDatabase): CycleRecordDao = db.cycleRecordDao()

    @Provides
    @Singleton
    fun provideInjectionDao(db: AppDatabase): InjectionDao = db.injectionDao()
}