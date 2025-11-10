package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.AppDatabase
import cz.tomasjanicek.bp.database.doctor.DoctorDao
import cz.tomasjanicek.bp.database.examination.ExaminationDao
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
}