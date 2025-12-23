package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder

// Jednoduchá simulace databáze v paměti, kterou sdílí všechny Fake repozitáře
object FakeDatabase {
    val doctors = mutableListOf<Doctor>()
    val examinations = mutableListOf<Examination>()

    // --- NOVÉ: Léky ---
    val medicines = mutableListOf<Medicine>()
    val reminders = mutableListOf<MedicineReminder>()

    fun clear() {
        doctors.clear()
        examinations.clear()
        medicines.clear()
        reminders.clear()
    }
}