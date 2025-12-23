package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.cycle.ICycleRepository
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.database.examination.ILocalExaminationsRepository
import cz.tomasjanicek.bp.database.injection.IInjectionRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementsRepository
import cz.tomasjanicek.bp.database.medicine.IMedicineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class TestRepositoryModule {

    // --- Naše hlavní Faky pro test (obsahují testovací data) ---
    @Binds
    abstract fun bindExaminationRepository(
        fake: FakeExaminationsRepository
    ): ILocalExaminationsRepository

    @Binds
    abstract fun bindInjectionRepository(
        fake: FakeInjectionRepository
    ): IInjectionRepository

    // --- "Vycpávkové" Faky pro ostatní ViewModely (aby build nespadl) ---
    @Binds
    abstract fun bindDoctorsRepository(
        fake: FakeDoctorsRepository
    ): ILocalDoctorsRepository

    @Binds
    abstract fun bindMedicineRepository(
        fake: FakeMedicineRepository
    ): IMedicineRepository

    @Binds
    abstract fun bindMeasurementCategoriesRepository(
        fake: FakeMeasurementCategoriesRepository
    ): ILocalMeasurementCategoriesRepository

    @Binds
    abstract fun bindMeasurementsRepository(
        fake: FakeMeasurementsRepository
    ): ILocalMeasurementsRepository

    @Binds
    abstract fun bindCycleRepository(
        fake: FakeCycleRepository
    ): ICycleRepository
}