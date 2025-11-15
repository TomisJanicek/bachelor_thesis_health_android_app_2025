package cz.tomasjanicek.bp.navigation

import androidx.navigation.NavController

class NavigationRouterImpl(private val navController: NavController): INavigationRouter {
    override fun navigateToDemoScreen() {
        navController.navigate(Destination.DemoScreen.route)
    }

    override fun navigaTetoListOfExaminationView() {
        navController.navigate(Destination.ListOfExaminationView.route)
    }
}