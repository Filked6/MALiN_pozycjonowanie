package pl.filked.malin_pozycjonowanie.data.mappers

import pl.filked.malin_pozycjonowanie.data.dto.BeaconDto
import pl.filked.malin_pozycjonowanie.domain.model.Beacon

fun BeaconDto.toDomain() = Beacon(
    uid = uid,
    name = name,
    longitude = longitude,
    latitude = latitude,
    floorId = floorId
)