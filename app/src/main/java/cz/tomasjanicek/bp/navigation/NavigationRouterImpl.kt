package cz.tomasjanicek.bp.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class NavigationRouterImpl(private val navController: NavController): INavigationRouter {
    override fun navigateToDemoScreen() {
        navController.navigate(Destination.DemoScreen.route)
    }

    override fun navigaTetoListOfExaminationView() {
        navController.navigate(Destination.ListOfExaminationView.route)
    }

    override fun navigateToAddEditExaminationScreen(id: Long?) {
        if (id != null){
            navController.navigate(Destination.AddEditExaminationScreen.route + "/$id")
        } else {
            navController.navigate(Destination.AddEditExaminationScreen.route)
        }
    }

    override fun navigateToExaminationDetail(id: Long?) {
        navController.navigate(Destination.DetailOfExaminationScreen.route + "/$id")
    }

    override fun navigateToDoctorEditScreen(id: Long?) {
        navController.navigate(Destination.DoctorEditScreen.route + "/$id")
    }

    override fun returBack() {
        navController.popBackStack()
    }

    override fun navigateToMapSelectorScreen(
        initialLatitude: Double?,
        initialLongitude: Double?
    ) {
        // Jednoduchá a čistá cesta s argumenty
        val route = if (initialLatitude != null && initialLongitude != null) {
            "${Destination.MapSelectorScreen.route}?lat=${initialLatitude.toFloat()}&lng=${initialLongitude.toFloat()}"
        } else {
            Destination.MapSelectorScreen.route
        }
        navController.navigate(route)
    }

    override fun returnWithResult(vararg results: Pair<String, Any>) {
        // Získáme předchozí obrazovku v navigačním zásobníku
        val previousBackStackEntry = navController.previousBackStackEntry
        // Uložíme výsledky do jejího SavedStateHandle
        results.forEach { (key, value) ->
            previousBackStackEntry?.savedStateHandle?.set(key, value)
        }
        // Vrátíme se zpět
        navController.popBackStack()
    }
}