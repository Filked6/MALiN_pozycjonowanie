package pl.filked.malin_pozycjonowanie.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arcgismaps.Color
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.arcgismaps.toolkit.geoviewcompose.MapViewProxy
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import pl.filked.malin_pozycjonowanie.data.ResultState
import pl.filked.malin_pozycjonowanie.data.RetrofitClient

@Composable
fun MapScreen() {
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(RetrofitClient.qrRepository)
    )

    val locationState by viewModel.locationState.collectAsState()

    val map = remember { createArcGisMap() }
    val mapViewProxy = remember { MapViewProxy() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val graphicsOverlay = remember { GraphicsOverlay() }
    val graphicsOverlays = remember { listOf(graphicsOverlay) }

    val qrLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null) {
            viewModel.processQrCode(result.contents)
        }
    }

    LaunchedEffect(locationState) {
        when (val state = locationState) {
            is ResultState.Success -> {
                val position = state.data

                val pointPUWG = Point(
                    x = position.longitude,
                    y = position.latitude,
                    spatialReference = SpatialReference(2180)
                )

                val pointWgs84 = GeometryEngine.projectOrNull(
                    geometry = pointPUWG,
                    spatialReference = SpatialReference.wgs84()
                )

                if (pointWgs84 == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Błąd: nie udało się przeliczyć współrzędnych")
                    }
                    return@LaunchedEffect
                }

                graphicsOverlay.graphics.clear()
                graphicsOverlay.graphics.add(
                    Graphic(
                        geometry = pointWgs84,
                        symbol = SimpleMarkerSymbol(
                            style = SimpleMarkerSymbolStyle.Circle,
                            color = Color.red,
                            size = 14f
                        )
                    )
                )

                scope.launch {
                    mapViewProxy.setViewpoint(
                        Viewpoint(
                            latitude = pointWgs84.y,
                            longitude = pointWgs84.x,
                            scale = 5000.0
                        )
                    )
                }
            }
            is ResultState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Błąd: ${state.throwable.localizedMessage ?: "Nieznany błąd"}"
                    )
                }
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    qrLauncher.launch(ScanOptions().apply {
                        setPrompt("Zeskanuj kod QR")
                        setBeepEnabled(true)
                    })
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Skanuj QR"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MapView(
                modifier = Modifier.fillMaxSize(),
                arcGISMap = map,
                mapViewProxy = mapViewProxy,
                graphicsOverlays = graphicsOverlays
            )

            if (locationState is ResultState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                )
            }
        }
    }
}