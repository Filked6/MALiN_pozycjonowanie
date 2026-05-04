package pl.filked.malin_pozycjonowanie.data.dto

data class DataClassApiResponse(
    val features: List<Feature>
)

data class Feature(
    val geometry: Geometry
)

data class Geometry(
    val x: Double,
    val y: Double
)