package pl.filked.malin_pozycjonowanie.data.repository

import pl.filked.malin_pozycjonowanie.domain.model.Position
import pl.filked.malin_pozycjonowanie.data.LocationInterface
import pl.filked.malin_pozycjonowanie.data.ResultState


class QrRepository(
    private val api: LocationInterface,

) {

    suspend fun getPosition(
        qrText: String
    ): ResultState<Position> {
        return try {

            val response = api.getLocationData(
                query = "qr_text='$qrText'"
            )

            val feature = response.features.firstOrNull()
            if (feature == null) {
                ResultState.Error(
                    Exception("Feature is null")
                )
            } else {
                ResultState.Success(
                    Position(
                        id = feature.attributes.id,
                        latitude = feature.geometry.y,
                        longitude = feature.geometry.x
                    )
                )
            }

        } catch (exception: Exception) {
            ResultState.Error(exception)
        }

    }

    suspend fun getPositionById(id: Int): ResultState<Position> {
        return try {
            val response = api.getLocationData(
                query = "id=$id"
            )

            val feature = response.features.firstOrNull()
            if (feature == null) {
                ResultState.Error(
                    Exception("Nie znaleziono w bazie obiektu o ID: $id")
                )
            } else {
                ResultState.Success(
                    Position(
                        id = feature.attributes.id,
                        latitude = feature.geometry.y,
                        longitude = feature.geometry.x
                    )
                )
            }
        } catch (exception: Exception) {
            ResultState.Error(exception)
        }
    }

}