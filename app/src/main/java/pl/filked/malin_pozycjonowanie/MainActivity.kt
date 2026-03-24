package pl.filked.malin_pozycjonowanie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pl.filked.malin_pozycjonowanie.data.dataSources.BeaconDataSource
import pl.filked.malin_pozycjonowanie.domain.model.Beacon
import pl.filked.malin_pozycjonowanie.ui.MainViewModel
import pl.filked.malin_pozycjonowanie.ui.theme.MALiN_pozycjonowanieTheme

class MainActivity : ComponentActivity(){
    companion object {
        private const val BEACONS_FILE_NAME = "beacons.json"
    }

    private val dataSource = BeaconDataSource(
        inputStreamProvider = { assets.open(BEACONS_FILE_NAME) }
    )
    private val viewModel = MainViewModel(dataSource)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MALiN_pozycjonowanieTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BeaconList(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)

                    )
                }
            }
        }
    }
}

@Composable
fun BeaconList (
    viewModel: MainViewModel,
    modifier: Modifier
) {
    val beacons = viewModel.beacons.collectAsState()

    LazyColumn(modifier) {
        items(beacons.value) { beacon: Beacon ->
            Text(
                text = beacon.name,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}