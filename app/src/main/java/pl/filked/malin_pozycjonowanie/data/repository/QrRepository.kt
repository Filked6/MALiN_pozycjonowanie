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
            val response = api.getLocationData(query = "qr_text='$qrText'")
            val geometry = response.features.firstOrNull()?.geometry
            if (geometry == null){
                ResultState.Error(Exception("Geometry is null"))
            } else {
                ResultState.Success(Position(geometry.y, geometry.x))
            }

        } catch (exception: Exception) {
            ResultState.Error(exception)
        }
    }
}