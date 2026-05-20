package pl.filked.malin_pozycjonowanie.ui

import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint

fun createArcGisMap(): ArcGISMap {
    return ArcGISMap(BasemapStyle.ArcGISStreets).apply {
        initialViewpoint = Viewpoint(
            latitude = 52.2298,
            longitude = 21.0117,
            scale = 72_000.0
        )
    }
}