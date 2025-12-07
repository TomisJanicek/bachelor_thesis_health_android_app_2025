package cz.tomasjanicek.bp.navigation

import kotlinx.coroutines.flow.Flow

interface INavigationRouter {

    fun navigateToDemoScreen()

    fun navigaTetoListOfExaminationView()
    fun navigateToAddEditExaminationScreen(id: Long?)
    fun navigateToExaminationDetail(id: Long?)
    fun navigateToDoctorEditScreen(id: Long?)

    fun returBack()


    fun navigateToMapSelectorScreen(initialLatitude: Double? = null, initialLongitude: Double? = null)
    fun returnWithResult(vararg results: Pair<String, Any>)

// 🔹 NOVÉ – měření

    /** Seznam kategorií měření (hlavní screen sekce měření). */
    fun navigateToListOfMeasurementCategories()

    /** Přidat / upravit kategorii měření. */
    fun navigateToAddEditMeasurementCategory(id: Long? = null)

    /** Přidat / upravit konkrétní měření v dané kategorii. */
    fun navigateToAddEditMeasurement(categoryId: Long, measurementId: Long? = null)

    /** Detail jednoho měření. */
    fun navigateToMeasurementDetail(id: Long)

    fun navigateToMeasurementCategoryDetail(categoryId: Long)


    /** Naviguje na hlavní obrazovku se seznamem léků. */
    fun navigateToMedicineList()

    /** Naviguje na obrazovku pro přidání/úpravu léku. */
    fun navigateToAddEditMedicine(medicineId: Long? = null)

    /** Naviguje na obrazovku se statistikami. */
    fun navigateToStatsScreen()

    /** Naviguje na obrazovku pro sledování cyklu. */
    fun navigateToCycleScreen()

    /** Naviguje na obrazovku pro přidání/editaci očkování. */
    fun navigateToAddEditInjectionScreen(injectionId: Long?)
}