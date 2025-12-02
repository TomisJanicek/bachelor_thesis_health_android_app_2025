package cz.tomasjanicek.bp.navigation

interface INavigationRouter {

    fun navigateToDemoScreen()

    fun navigaTetoListOfExaminationView()
    fun navigateToAddEditExaminationScreen(id: Long?)
    fun navigateToExaminationDetail(id: Long?)

    fun returBack()
}