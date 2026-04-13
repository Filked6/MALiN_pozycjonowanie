package pl.filked.malin_pozycjonowanie

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
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
    val context = LocalContext.current
    val zxingScannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result: ScanIntentResult ->
        val qrContent = result.contents

        if (qrContent.isNullOrBlank()) {
            //viewModel.cancelQrScanning()
        } else {
            //viewModel.handleQrContent(qrContent)
        }
        Toast.makeText(context, qrContent, Toast.LENGTH_LONG).show()
    }

    Button(
        onClick = {zxingScannerLauncher.launch(ScanOptions())},
        modifier = Modifier
            .padding(top = 40.dp)
    ){
        Text(
            text = "Skanuj QR CODE"
        )
    }
}
