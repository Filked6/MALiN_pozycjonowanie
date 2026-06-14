package pl.filked.malin_pozycjonowanie.ui

import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.arcgismaps.Color
import com.arcgismaps.geometry.Geometry
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.Polyline
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
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
fun MapScreen(
    lat: Double,
    lon: Double,
    scale: Double
) {

    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(RetrofitClient.qrRepository)
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationState by viewModel.locationState.collectAsState()

    val map = remember(lat, lon, scale) { createArcGisMap(lat, lon, scale) }
    val mapViewProxy = remember { MapViewProxy() }

    val polygonsOverlay = remember { GraphicsOverlay() }
    val markerOverlay = remember { GraphicsOverlay() }
    val graphicsOverlays = remember { listOf(polygonsOverlay, markerOverlay) }

    val artworks = remember { loadArtworks(context) }

    var selectedArtwork by remember { mutableStateOf<Artwork?>(null) }
    var sheetVisible by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedArtworkFromList by remember { mutableStateOf(artworks.firstOrNull()) }
    var isSearchFromList by remember { mutableStateOf(false) }

    var userLocationPoint by remember { mutableStateOf<Point?>(null) }

    val qrLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        result.contents?.let {
            isSearchFromList = false
            viewModel.processQrCode(it)
        }
    }

    LaunchedEffect(Unit) {
        try {
            val rawJsonString = context.assets.open("parter.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = org.json.JSONObject(rawJsonString)

            val fillSymbol = SimpleFillSymbol(
                style = SimpleFillSymbolStyle.Solid,
                color = Color("#DDCAD9".toColorInt()),
                outline = SimpleLineSymbol(
                    style = SimpleLineSymbolStyle.Solid,
                    color = Color("#7C616C".toColorInt()),
                    width = 2f
                )
            )

            if (jsonObject.has("features")) {
                val featuresArray = jsonObject.getJSONArray("features")
                for (i in 0 until featuresArray.length()) {
                    val feature = featuresArray.getJSONObject(i)
                    if (feature.has("geometry")) {
                        val geometryJson = feature.getJSONObject("geometry")
                        val sr = org.json.JSONObject()
                        sr.put("wkid", 2180)
                        geometryJson.put("spatialReference", sr)

                        val fixedJsonString = geometryJson.toString()
                        val geometry = Geometry.fromJsonOrNull(fixedJsonString)

                        if (geometry != null) {
                            polygonsOverlay.graphics.add(Graphic(geometry, fillSymbol))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MY_DEBUG", "Błąd odczytu pliku parter.json: ${e.message}")
        }
    }

    Box {
        LaunchedEffect(locationState) {
            when (val state = locationState) {
                is ResultState.Success -> {
                    val position = state.data
                    val pointPUWG = Point(
                        x = position.longitude,
                        y = position.latitude,
                        spatialReference = SpatialReference(2180)
                    )

                    val fetchedPointWgs84 = GeometryEngine.projectOrNull(
                        geometry = pointPUWG,
                        spatialReference = SpatialReference.wgs84()
                    ) ?: return@LaunchedEffect

                    selectedArtwork = artworks.find { it.id == position.id }
                    val currentSearchWasFromList = isSearchFromList

                    markerOverlay.graphics.clear()

                    if (!currentSearchWasFromList) {
                        userLocationPoint = fetchedPointWgs84
                        sheetVisible = true

                        markerOverlay.graphics.add(
                            Graphic(
                                geometry = fetchedPointWgs84,
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
                                    latitude = fetchedPointWgs84.y,
                                    longitude = fetchedPointWgs84.x,
                                    scale = 5000.0
                                )
                            )
                        }

                    } else {

                        sheetVisible = false

                        userLocationPoint?.let { userPt ->

                            markerOverlay.graphics.add(
                                Graphic(
                                    geometry = userPt,
                                    symbol = SimpleMarkerSymbol(
                                        style = SimpleMarkerSymbolStyle.Circle,
                                        color = Color.red,
                                        size = 14f
                                    )
                                )
                            )

                            val navigationLine = Polyline(listOf(userPt, fetchedPointWgs84))
                            val lineSymbol = SimpleLineSymbol(
                                style = SimpleLineSymbolStyle.Dash,
                                color = Color("#000000".toColorInt()),
                                width = 3f
                            )
                            markerOverlay.graphics.add(
                                Graphic(
                                    geometry = navigationLine,
                                    symbol = lineSymbol
                                )
                            )
                        }

                        markerOverlay.graphics.add(
                            Graphic(
                                geometry = fetchedPointWgs84,
                                symbol = SimpleMarkerSymbol(
                                    style = SimpleMarkerSymbolStyle.Circle,
                                    color = Color.blue,
                                    size = 14f
                                )
                            )
                        )

                        scope.launch {
                            mapViewProxy.setViewpoint(
                                Viewpoint(
                                    latitude = fetchedPointWgs84.y,
                                    longitude = fetchedPointWgs84.x,
                                    scale = 5000.0
                                )
                            )
                        }
                    }

                    isSearchFromList = false
                }

                is ResultState.Error -> {
                    selectedArtwork = null
                    sheetVisible = !isSearchFromList
                    isSearchFromList = false
                }

                else -> Unit
            }
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedArtworkFromList?.title ?: "Wybierz obraz...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            artworks.forEach { artwork ->
                                DropdownMenuItem(
                                    text = { Text(artwork.title) },
                                    onClick = {
                                        selectedArtworkFromList = artwork
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            selectedArtworkFromList?.let { artwork ->
                                isSearchFromList = true
                                viewModel.processArtworkId(artwork.id)
                            }
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Pokaż na mapie",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (locationState is ResultState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (sheetVisible) {
                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = { sheetVisible = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        if (selectedArtwork == null) {
                            Text(text = "Brak obrazu", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Nie znaleziono dopasowanego obiektu w bazie.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(text = selectedArtwork?.title ?: "", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(selectedArtwork?.image)
                                    .addHeader("User-Agent", "MalinAPP by TripTropTeam 1.0")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                when (painter.state) {
                                    is AsyncImagePainter.State.Loading -> {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator()
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Ładowanie obrazu...")
                                            }
                                        }
                                    }
                                    is AsyncImagePainter.State.Error -> Text("Nie udało się załadować obrazu")
                                    else -> SubcomposeAsyncImageContent()
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = selectedArtwork?.description ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}