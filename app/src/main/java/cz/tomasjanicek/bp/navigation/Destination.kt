package cz.tomasjanicek.bp.navigation

sealed class Destination(val route: String) {
    object ListOfExaminationView : Destination("list_of_examination_view")

    object AddEditExaminationScreen : Destination("add_edit_examination_screen")

    object DetailOfExaminationScreen : Destination("detail_of_examination_screen")

    object DoctorEditScreen : Destination("doctor_edit_screen")

    object MapSelectorScreen: Destination("map_selector")

    object ListOfMeasurementCategoryScreen :
        Destination("list_of_measurement_category_screen")

    object AddEditMeasurementCategoryScreen :
        Destination("add_edit_measurement_category_screen")

    object AddEditMeasurementScreen :
        Destination("add_edit_measurement_screen")

    object DetailOfMeasurementScreen :
        Destination("detail_of_measurement_screen")

    object MeasurementCategoryDetailScreen :
        Destination("measurement_category_detail_screen")

    /** Seznam dnešních připomínek léků. */
    object MedicineListScreen : Destination("medicine_list_screen")

    /** Obrazovka pro přidání nebo úpravu léku. */
    object AddEditMedicineScreen : Destination("add_edit_medicine_screen")

    /** NOVÁ DESTINACE: Obrazovka pro statistiky. */
    object StatsScreen : Destination("stats_screen")

    /** NOVÁ DESTINACE: Obrazovka pro sledování cyklu. */
    object CycleScreen : Destination("cycle_screen")
    /** NOVÁ DESTINACE: Obrazovka pro přidání/editaci očkování. */
    object AddEditInjectionScreen : Destination("add_edit_injection_screen")

    object SplashScreen : Destination("splash_screen")
    object LoginScreen : Destination("login_screen")
    object UserScreen : Destination("user_screen")
    object SettingsScreen : Destination("settings_screen")

    object DefaultCategories : Destination("measurement/default_categories")

}