package cz.tomasjanicek.bp.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cz.tomasjanicek.bp.ui.screens.DemoScreen
import cz.tomasjanicek.bp.ui.screens.examination.addEdit.AddEditExaminationScreen
import cz.tomasjanicek.bp.ui.screens.examination.detail.DetailOfExaminationScreen
import cz.tomasjanicek.bp.ui.screens.examination.doctorEdit.DoctorEditScreen
import cz.tomasjanicek.bp.ui.screens.examination.list.ListOfExaminationScreen
import cz.tomasjanicek.bp.ui.screens.examination.mapSelector.MapSelectorScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    navigationRouter: INavigationRouter = remember {
        NavigationRouterImpl(navController)
    },
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destination.DemoScreen.route) {
            DemoScreen(navigationRouter = navigationRouter, currentScreenIndex = 4)
        }

        composable(Destination.ListOfExaminationView.route) {
            ListOfExaminationScreen(navigationRouter = navigationRouter, currentScreenIndex = 0)

        }
        composable(Destination.AddEditExaminationScreen.route) {
            AddEditExaminationScreen(navigationRouter = navigationRouter, id = null)
        }
        composable(
            Destination.AddEditExaminationScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )) {
            val id = it.arguments?.getLong("id")
            AddEditExaminationScreen(navigationRouter = navigationRouter, id = id)

        }
        composable(
            route = Destination.DetailOfExaminationScreen.route + "/{id}",
            arguments = listOf(navArgument("id") {type = NavType.LongType})) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: -1L
            DetailOfExaminationScreen(navigationRouter = navigationRouter, doctorId = id)
        }
        composable(
            route = Destination.DoctorEditScreen.route + "/{id}",
            arguments = listOf(navArgument("id") {type = NavType.LongType})
        ) { backStackEntry ->
            // ZDE JE KLÍČOVÁ ZMĚNA PRO PŘIJETÍ VÝSLEDKU
            // Získáme referenci na výsledek z SavedStateHandle
            val latitudeResult = backStackEntry.savedStateHandle.get<Double>("latitude")
            val longitudeResult = backStackEntry.savedStateHandle.get<Double>("longitude")
            Log.d("LocationFlow", "[NavGraph] Přijat výsledek: lat=${latitudeResult}, lng=${longitudeResult}")
            val id = backStackEntry.arguments?.getLong("id") ?: -1L
            DoctorEditScreen(
                navigationRouter = navigationRouter,
                doctorId = id,
                // Předáme výsledek přímo do obrazovky
                latitudeFromResult = latitudeResult,
                longitudeFromResult = longitudeResult,
                // Po zpracování výsledek "vyčistíme", aby se nespustil znovu
                onResultConsumed = {
                    Log.d("LocationFlow", "[NavGraph] Konzumuji a mažu výsledek.")
                    backStackEntry.savedStateHandle.remove<Double>("latitude")
                    backStackEntry.savedStateHandle.remove<Double>("longitude")
                }
            )
        }
        composable(
            route = Destination.MapSelectorScreen.route + "?lat={lat}&lng={lng}",
            arguments = listOf(
                navArgument("lat") {
                    type = NavType.FloatType
                    defaultValue = -1.0f // Výchozí hodnota, pokud není předáno
                },
                navArgument("lng") {
                    type = NavType.FloatType
                    defaultValue = -1.0f
                }
            )
        ) { backStackEntry ->
            val latArg = backStackEntry.arguments?.getFloat("lat") ?: -1.0f
            val lngArg = backStackEntry.arguments?.getFloat("lng") ?: -1.0f

            // Převedeme na Double? (nullable) - pokud je hodnota výchozí, bude null
            val initialLatitude = if (latArg != -1.0f) latArg.toDouble() else null
            val initialLongitude = if (lngArg != -1.0f) lngArg.toDouble() else null

            MapSelectorScreen( //TODO červeně podtrženo
                navigationRouter = navigationRouter,
                initialLatitude = initialLatitude,
                initialLongitude = initialLongitude
            )
        }
    }
}