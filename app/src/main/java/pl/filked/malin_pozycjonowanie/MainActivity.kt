package pl.filked.malin_pozycjonowanie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

                    composable(
                        route = "map_screen/{lat}/{lon}/{scale}",
                        arguments = listOf(
                            navArgument("lat") { type = NavType.StringType },
                            navArgument("lon") { type = NavType.StringType },
                            navArgument("scale") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 52.2298
                        val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 21.0117
                        val scale = backStackEntry.arguments?.getString("scale")?.toDoubleOrNull() ?: 5000.0

                        MapScreen(lat = lat, lon = lon, scale = scale)
                    }
                }
            }
        }
    }
}