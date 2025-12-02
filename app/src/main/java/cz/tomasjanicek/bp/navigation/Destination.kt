package cz.tomasjanicek.bp.navigation

sealed class Destination(val route: String) {
    object ListOfExaminationView : Destination("list_of_examination_view")
    object DemoScreen : Destination("demo_screen")

    object AddEditExaminationScreen : Destination("add_edit_examination_screen")

    object DetailOfExaminationScreen : Destination("detail_of_examination_screen")




}