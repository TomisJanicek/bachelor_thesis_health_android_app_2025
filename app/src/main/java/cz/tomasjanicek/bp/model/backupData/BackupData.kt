package cz.tomasjanicek.bp.model.backupData

import cz.tomasjanicek.bp.model.*

/**
 * Tato třída reprezentuje kompletní otisk databáze pro JSON export.
 */
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),

    // Data
    val doctors: List<Doctor> = emptyList(),
    val examinations: List<Examination> = emptyList(),
    val measurementCategories: List<MeasurementCategory> = emptyList(),
    val measurementFields: List<MeasurementCategoryField> = emptyList(),
    val measurements: List<Measurement> = emptyList(),
    val measurementValues: List<MeasurementValue> = emptyList(),
    val medicines: List<Medicine> = emptyList(),
    val medicineReminders: List<MedicineReminder> = emptyList(),
    val cycleRecords: List<CycleRecord> = emptyList(),
    val injections: List<Injection> = emptyList()
)