package cz.tomasjanicek.bp.navigation

sealed class Destination(val route: String) {
    object ListOfExaminationView : Destination("list_of_examination_view")
    object DemoScreen : Destination("demo_screen")

    object AddEditExaminationScreen : Destination("add_edit_examination_screen")

    object DetailOfExaminationScreen : Destination("detail_of_examination_screen")

    object DoctorEditScreen : Destination("doctor_edit_screen")

    object MapSelectorScreen: Destination("map_selector")

    // 🔹 NOVÉ – měření
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
}
