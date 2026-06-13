package pl.filked.malin_pozycjonowanie.ui

import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import pl.filked.malin_pozycjonowanie.data.ResultState
import pl.filked.malin_pozycjonowanie.data.RetrofitClient
import pl.filked.malin_pozycjonowanie.domain.model.Position

data class Artwork(
    val id: Int,
    val title: String,
    val description: String,
    val image: String
)

fun loadArtworks(context: Context): List<Artwork> {
    val json = context.assets.open("obrazy.json")
        .bufferedReader()
        .use { it.readText() }

    return Gson().fromJson(json, object : TypeToken<List<Artwork>>() {}.type)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {

    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(RetrofitClient.qrRepository)
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationState by viewModel.locationState.collectAsState()

    val map = remember { createArcGisMap() }
    val mapViewProxy = remember { MapViewProxy() }

    val graphicsOverlay = remember { GraphicsOverlay() }
    val graphicsOverlays = remember { listOf(graphicsOverlay) }

    val artworks = remember { loadArtworks(context) }

    var selectedArtwork by remember { mutableStateOf<Artwork?>(null) }
    var sheetVisible by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val qrLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        result.contents?.let {
            viewModel.processQrCode(it)
        }
    }

    LaunchedEffect(locationState) {
        when (val state = locationState) {

            is ResultState.Success -> {
                val position = state.data

                Log.d("MY_DEBUG", "Position received: $position")

                val pointPUWG = Point(
                    x = position.longitude,
                    y = position.latitude,
                    spatialReference = SpatialReference(2180)
                )

                val pointWgs84 = GeometryEngine.projectOrNull(
                    geometry = pointPUWG,
                    spatialReference = SpatialReference.wgs84()
                ) ?: return@LaunchedEffect

                selectedArtwork = artworks.find { it.id == position.id }
                sheetVisible = true

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
                selectedArtwork = null
                sheetVisible = true
            }

            else -> Unit
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    qrLauncher.launch(
                        ScanOptions().apply {
                            setPrompt("Zeskanuj QR")
                            setBeepEnabled(true)
                        }
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "QR"
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            MapView(
                modifier = Modifier.fillMaxSize(),
                arcGISMap = map,
                mapViewProxy = mapViewProxy,
                graphicsOverlays = graphicsOverlays
            )

            if (locationState is ResultState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (sheetVisible) {

                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = {
                        sheetVisible = false
                    }
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {

                        if (selectedArtwork == null) {

                            Text(
                                text = "Brak obrazu",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Nie znaleziono dopasowanego obiektu dla tego kodu QR.",
                                style = MaterialTheme.typography.bodyMedium
                            )

                        } else {

                            Text(
                                text = selectedArtwork?.title ?: "",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Log.d("MY_DEBUG", selectedArtwork?.image ?: "NULL IMAGE")
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(selectedArtwork?.image)
                                    .addHeader(
                                        "User-Agent",
                                        "MalinAPP by TripTropTeam 1.0"
                                    )
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {

                                when (painter.state) {

                                    is AsyncImagePainter.State.Loading -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator()
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Ładowanie obrazu...")
                                            }
                                        }
                                    }

                                    is AsyncImagePainter.State.Error -> {
                                        Text("Nie udało się załadować obrazu")
                                    }

                                    else -> {
                                        SubcomposeAsyncImageContent()
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = selectedArtwork?.description ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}