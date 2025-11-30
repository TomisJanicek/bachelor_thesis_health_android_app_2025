package cz.tomasjanicek.bp.navigation

import androidx.navigation.NavController

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

    override fun returBack() {
        navController.popBackStack()
    }
}