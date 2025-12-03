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

}