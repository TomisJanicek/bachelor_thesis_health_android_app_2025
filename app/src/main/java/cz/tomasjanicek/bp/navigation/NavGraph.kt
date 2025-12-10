package cz.tomasjanicek.bp.navigation

import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import cz.tomasjanicek.bp.auth.AuthRepository
import cz.tomasjanicek.bp.auth.LoginScreen
import cz.tomasjanicek.bp.ui.screens.cycle.CycleScreen
import cz.tomasjanicek.bp.ui.screens.examination.addEdit.AddEditExaminationScreen
import cz.tomasjanicek.bp.ui.screens.examination.detail.DetailOfExaminationScreen
import cz.tomasjanicek.bp.ui.screens.examination.doctorEdit.DoctorEditScreen
import cz.tomasjanicek.bp.ui.screens.examination.list.ListOfExaminationScreen
import cz.tomasjanicek.bp.ui.screens.examination.mapSelector.MapSelectorScreen
import cz.tomasjanicek.bp.ui.screens.injection.AddEditInjectionScreen
import cz.tomasjanicek.bp.ui.screens.measurement.addEditCategory.AddEditCategoryScreen
import cz.tomasjanicek.bp.ui.screens.measurement.addEditMeasurement.AddEditMeasurementScreen
import cz.tomasjanicek.bp.ui.screens.measurement.categoryDetail.MeasurementCategoryDetailScreen
import cz.tomasjanicek.bp.ui.screens.measurement.list.ListOfMeasurementCategory
import cz.tomasjanicek.bp.ui.screens.medicine.addEdit.AddEditMedicineScreen
import cz.tomasjanicek.bp.ui.screens.medicine.list.MedicineListScreen
import cz.tomasjanicek.bp.ui.screens.splash.SplashScreen
import cz.tomasjanicek.bp.ui.screens.stats.StatsScreen
import cz.tomasjanicek.bp.ui.screens.user.UserScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    navigationRouter: INavigationRouter = remember {
        NavigationRouterImpl(navController)
    },
    startDestination: String,
    repository: AuthRepository
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Destination.CycleScreen.route) {
            CycleScreen(navigationRouter = navigationRouter, currentScreenIndex = 4)
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                MapSelectorScreen( //TODO červeně podtrženo
                    navigationRouter = navigationRouter,
                    initialLatitude = initialLatitude,
                    initialLongitude = initialLongitude
                )
            }
        }
        // 🔹 NOVÉ – measurement routy

        // Seznam kategorií měření
        composable(Destination.ListOfMeasurementCategoryScreen.route) {
            ListOfMeasurementCategory(
                navigationRouter = navigationRouter
            )
        }

        // Přidat novou kategorii měření
        composable(Destination.AddEditMeasurementCategoryScreen.route) {
            AddEditCategoryScreen(
                navigationRouter = navigationRouter,
                id = null
            )
        }

        // Upravit existující kategorii
        composable(
            route = Destination.AddEditMeasurementCategoryScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val idArg = backStackEntry.arguments?.getLong("id") ?: -1L
            val id = if (idArg == -1L) null else idArg
            AddEditCategoryScreen(
                navigationRouter = navigationRouter,
                id = id
            )
        }

        // Přidat nové měření v kategorii
        composable(
            route = Destination.AddEditMeasurementScreen.route + "/{categoryId}",
            arguments = listOf(
                navArgument("categoryId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: -1L
            AddEditMeasurementScreen(
                navigationRouter = navigationRouter,
                categoryId = categoryId,
                measurementId = null
            )
        }

        // Upravit existující měření
        composable(
            route = Destination.AddEditMeasurementScreen.route + "/{categoryId}/{measurementId}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("measurementId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: -1L
            val measurementIdArg = backStackEntry.arguments?.getLong("measurementId") ?: -1L
            val measurementId = if (measurementIdArg == -1L) null else measurementIdArg

            AddEditMeasurementScreen(
                navigationRouter = navigationRouter,
                categoryId = categoryId,
                measurementId = measurementId
            )
        }

        composable(
            route = Destination.MeasurementCategoryDetailScreen.route + "/{categoryId}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: -1L
            MeasurementCategoryDetailScreen(
                navigationRouter = navigationRouter,
                categoryId = categoryId
            )
        }
        // Hlavní obrazovka léků
        composable(Destination.MedicineListScreen.route) {
            MedicineListScreen(navigationRouter = navigationRouter, currentScreenIndex = 2) // TODO: index podle bottom baru
        }

        // Obrazovka pro přidání léku
        composable(Destination.AddEditMedicineScreen.route) {
            AddEditMedicineScreen(navigationRouter = navigationRouter, medicineId = null)
        }

        // Obrazovka pro úpravu léku
        composable(
            route = Destination.AddEditMedicineScreen.route + "/{medicineId}",
            arguments = listOf(
                navArgument("medicineId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("medicineId")
            AddEditMedicineScreen(navigationRouter = navigationRouter, medicineId = id)
        }

        composable(Destination.StatsScreen.route) {
            StatsScreen(navigationRouter = navigationRouter, currentScreenIndex = 3)
        }
        composable(Destination.AddEditInjectionScreen.route) {
            AddEditInjectionScreen(
                navigationRouter = navigationRouter,
                injectionId = null
            )
        }

        // Obrazovka pro úpravu existujícího očkování
        composable(
            route = Destination.AddEditInjectionScreen.route + "/{injectionId}",
            arguments = listOf(
                navArgument("injectionId") {
                    type = NavType.LongType
                    // Výchozí hodnota pro případ, že by argument chyběl,
                    // i když by se to u této cesty stát nemělo.
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val idArg = backStackEntry.arguments?.getLong("injectionId") ?: -1L
            // Pokud je ID -1 (výchozí), předáme null, což značí nový záznam.
            // Jinak předáme skutečné ID.
            val id = if (idArg == -1L) null else idArg
            AddEditInjectionScreen(
                navigationRouter = navigationRouter,
                injectionId = id
            )
        }

        // --- 1. SPLASH SCREEN ---
        composable(Destination.SplashScreen.route) {
            SplashScreen(
                navigationRouter = navigationRouter,
                repository = repository
            )
        }

        // --- 2. LOGIN SCREEN ---
        composable(Destination.LoginScreen.route) {
            // Zde použijeme LoginScreen, který jsi poslal v dotazu
            // LoginViewModel si to sosne samo přes hiltViewModel()
            LoginScreen(
                onLoginSuccess = {
                    navigationRouter.navigateToHomeFromLogin()
                }
            )
        }
        composable(Destination.UserScreen.route) {
            UserScreen(navigationRouter = navigationRouter)
        }
    }
}