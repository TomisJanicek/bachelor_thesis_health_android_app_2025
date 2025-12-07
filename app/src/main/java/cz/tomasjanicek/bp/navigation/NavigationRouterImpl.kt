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

    // 🔹 NOVĚ – měření

    override fun navigateToListOfMeasurementCategories() {
        navController.navigate(Destination.ListOfMeasurementCategoryScreen.route)
    }

    override fun navigateToAddEditMeasurementCategory(id: Long?) {
        if (id != null && id != 0L) {
            navController.navigate(
                Destination.AddEditMeasurementCategoryScreen.route + "/$id"
            )
        } else {
            navController.navigate(Destination.AddEditMeasurementCategoryScreen.route)
        }
    }

    override fun navigateToAddEditMeasurement(categoryId: Long, measurementId: Long?) {
        val base = "${Destination.AddEditMeasurementScreen.route}/$categoryId"
        val route = if (measurementId != null && measurementId != 0L) {
            "$base/$measurementId"
        } else {
            base
        }
        navController.navigate(route)
    }

    override fun navigateToMeasurementDetail(id: Long) {
        navController.navigate("${Destination.DetailOfMeasurementScreen.route}/$id")
    }

    override fun navigateToMeasurementCategoryDetail(categoryId: Long) {
        navController.navigate(
            Destination.MeasurementCategoryDetailScreen.route + "/$categoryId"
        )
    }

    // --- DOPLNĚNO ---
    override fun navigateToMedicineList() {
        navController.navigate(Destination.MedicineListScreen.route)
    }

    override fun navigateToAddEditMedicine(medicineId: Long?) {
        val route = if (medicineId != null) {
            "${Destination.AddEditMedicineScreen.route}/$medicineId"
        } else {
            Destination.AddEditMedicineScreen.route
        }
        navController.navigate(route)
    }

    override fun navigateToStatsScreen() {
        navController.navigate(Destination.StatsScreen.route)
    }

    override fun navigateToCycleScreen() {
        navController.navigate(Destination.CycleScreen.route)
    }

    override fun navigateToAddEditInjectionScreen(injectionId: Long?) {
        val route = if (injectionId != null && injectionId != 0L) {
            "${Destination.AddEditInjectionScreen.route}/$injectionId"
        } else {
            Destination.AddEditInjectionScreen.route
        }
        navController.navigate(route)
    }
}