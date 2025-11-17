package cz.tomasjanicek.bp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.tomasjanicek.bp.ui.screens.DemoScreen
import cz.tomasjanicek.bp.ui.screens.examination.ListOfExaminationScreen

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
        composable(Destination.DemoScreen.route){
            DemoScreen(navigationRouter = navigationRouter, currentScreenIndex = 4)
        }

        composable(Destination.ListOfExaminationView.route) {
            ListOfExaminationScreen(navigationRouter = navigationRouter, currentScreenIndex = 0)

        }
    }
}