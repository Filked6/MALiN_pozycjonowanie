package pl.filked.malin_pozycjonowanie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import pl.filked.malin_pozycjonowanie.data.ResultState
import pl.filked.malin_pozycjonowanie.data.RetrofitClient
import pl.filked.malin_pozycjonowanie.ui.MainViewModel
import pl.filked.malin_pozycjonowanie.ui.MainViewModelFactory
import pl.filked.malin_pozycjonowanie.ui.theme.MALiN_pozycjonowanieTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MALiN_pozycjonowanieTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(RetrofitClient.qrRepository)
                )

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // Obserwacja stanów z ViewModelu
    val scannedText by viewModel.scannedText.collectAsState()
    val locationState by viewModel.locationState.collectAsState()

    // Rejestracja skanera
    val zxingScannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null) {
            viewModel.processQrCode(result.contents)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                zxingScannerLauncher.launch(ScanOptions())
            }
        ) {
            Text(text = "Skanuj QR CODE")
        }

        if (scannedText.isNotBlank()) {
            Text(
                text = "Zeskanowane ID: $scannedText",
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Obsługa stanu z serwera (Lokalizacja)
        when (val state = locationState) {
            is ResultState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
            is ResultState.Success -> {
                Text(
                    text = "Lokalizacja: X (Szer): ${state.data.longitude}, Y (Dł): ${state.data.latitude}",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            is ResultState.Error -> {
                Text(
                    text = "Błąd: ${state.throwable.localizedMessage}",
                    modifier = Modifier.padding(top = 16.dp),
                    color = androidx.compose.ui.graphics.Color.Red
                )
            }
            null -> {
                // Pusty stan początkowy
            }
        }
    }
}