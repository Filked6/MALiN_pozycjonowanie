package pl.filked.malin_pozycjonowanie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pl.filked.malin_pozycjonowanie.ui.MapScreen
import pl.filked.malin_pozycjonowanie.ui.theme.MALiN_pozycjonowanieTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MALiN_pozycjonowanieTheme {
                MapScreen()
            }
        }
    }
}