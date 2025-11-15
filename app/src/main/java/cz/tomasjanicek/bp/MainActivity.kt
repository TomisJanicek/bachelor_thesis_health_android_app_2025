package cz.tomasjanicek.bp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cz.tomasjanicek.bp.navigation.Destination
import cz.tomasjanicek.bp.navigation.NavGraph
import cz.tomasjanicek.bp.ui.screens.examination.ListOfExaminationView
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