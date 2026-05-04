package pl.filked.malin_pozycjonowanie

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult

@Composable
fun giveTheCoords(

){
    var scannedText by remember { mutableStateOf("") }
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

        val lastPhrase = qrContent.substringAfterLast("/")
        scannedText = lastPhrase
        Toast.makeText(context, qrContent, Toast.LENGTH_LONG).show()


    }
}