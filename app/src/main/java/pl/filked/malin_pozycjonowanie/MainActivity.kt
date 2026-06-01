package pl.filked.malin_pozycjonowanie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.filked.malin_pozycjonowanie.ui.ChooseScreen
import pl.filked.malin_pozycjonowanie.ui.MapScreen
import pl.filked.malin_pozycjonowanie.ui.theme.MALiN_pozycjonowanieTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MALiN_pozycjonowanieTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "choose_screen") {
                    composable("choose_screen") {
                        ChooseScreen(navController)
                    }
                    composable("map_screen") {
                        MapScreen()
                    }
                }
            }
        }
    }
}