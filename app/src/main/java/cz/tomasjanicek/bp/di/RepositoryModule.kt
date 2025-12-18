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
import cz.tomasjanicek.bp.services.BackupScheduler
import cz.tomasjanicek.bp.services.notification.AlarmScheduler
import cz.tomasjanicek.bp.ui.screens.settings.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // --- ZMĚNA ZDE: Přidány parametry alarmScheduler a settingsManager ---
    @Provides
    @Singleton
    fun provideExaminationsRepository(
        dao: ExaminationDao,
        doctorDao: DoctorDao,
        backupScheduler: BackupScheduler,
        alarmScheduler: AlarmScheduler, // <-- Nové
        settingsManager: SettingsManager // <-- Nové
    ): ILocalExaminationsRepository = LocalExaminationsRepositoryImpl(
        dao,
        doctorDao,
        backupScheduler,
        alarmScheduler,
        settingsManager
    )
    @Provides
    @Singleton
    fun provideDoctorsRepository(
        dao: DoctorDao,
        backupScheduler: BackupScheduler
    ): ILocalDoctorsRepository = LocalDoctorsRepositoryImpl(dao, backupScheduler)

    @Provides
    @Singleton
    fun provideMeasurementCategoriesRepository(
        dao: MeasurementCategoryDao,
        backupScheduler: BackupScheduler
    ): ILocalMeasurementCategoriesRepository =
        LocalMeasurementCategoriesRepositoryImpl(dao, backupScheduler)

    @Provides
    @Singleton
    fun provideMeasurementsRepository(
        dao: MeasurementDao,
        backupScheduler: BackupScheduler
    ): ILocalMeasurementsRepository =
        LocalMeasurementsRepositoryImpl(dao, backupScheduler)

    @Provides
    @Singleton
    fun provideMedicineRepository(
        dao: MedicineDao,
        backupScheduler: BackupScheduler,
        alarmScheduler: AlarmScheduler, // <-- Nové
        settingsManager: SettingsManager // <-- Nové
    ): IMedicineRepository = LocalMedicineRepositoryImpl(
        dao,
        backupScheduler,
        alarmScheduler,
        settingsManager
    )

    @Provides
    @Singleton
    fun provideCycleRepository(
        dao: CycleRecordDao,
        backupScheduler: BackupScheduler
    ): ICycleRepository = CycleRepositoryImpl(dao, backupScheduler)

    @Provides
    @Singleton
    fun provideInjectionRepository(
        dao: InjectionDao,
        backupScheduler: BackupScheduler
    ): IInjectionRepository = InjectionRepositoryImpl(dao, backupScheduler)

}