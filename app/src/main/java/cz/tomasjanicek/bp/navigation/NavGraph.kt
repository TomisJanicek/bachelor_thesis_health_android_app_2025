package cz.tomasjanicek.bp.navigation

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
import cz.tomasjanicek.bp.ui.screens.examination.list.ListOfExaminationScreen

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
    }
}