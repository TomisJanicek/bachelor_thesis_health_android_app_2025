package cz.tomasjanicek.bp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cz.tomasjanicek.bp.navigation.Destination
import cz.tomasjanicek.bp.navigation.NavGraph
import cz.tomasjanicek.bp.ui.theme.BpTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BpTheme {
                NavGraph(
                    startDestination = Destination.ListOfExaminationView.route
                )
            }
        }
    }
}