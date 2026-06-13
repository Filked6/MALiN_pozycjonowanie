package pl.filked.malin_pozycjonowanie.data.dto

data class DataClassApiResponse(
    val features: List<Feature>
)

data class Feature(
    val attributes: Attributes,
    val geometry: Geometry
)

data class Attributes(
    val id: Int,
)

data class Geometry(
    val x: Double,
    val y: Double
)