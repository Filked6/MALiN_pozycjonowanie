package pl.filked.malin_pozycjonowanie.domain.model

data class Beacon (
    val uid: String,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val floorId: Int
)