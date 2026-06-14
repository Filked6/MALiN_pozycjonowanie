package pl.filked.malin_pozycjonowanie.ui

import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
fun createArcGisMap(
     latitude: Double = 52.2298,
     longitude: Double = 21.0117,
     scale: Double = 8000.0
 ): ArcGISMap {
    return ArcGISMap(BasemapStyle.ArcGISStreets).apply {
        initialViewpoint = Viewpoint(
            latitude = latitude,
            longitude = longitude,
            scale = scale
        )
    }
}