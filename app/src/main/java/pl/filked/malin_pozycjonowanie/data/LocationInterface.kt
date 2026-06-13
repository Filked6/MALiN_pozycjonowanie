package pl.filked.malin_pozycjonowanie.data

import pl.filked.malin_pozycjonowanie.data.dto.DataClassApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationInterface{
    @GET("server/rest/services/SION2_Geoopisy/sion_topo_qrcode/MapServer/0/query")
    suspend fun getLocationData(
        @Query("where") query:String,
        @Query("outFields") outFields: String = "*",
        @Query("f") format: String = "json",
    ): DataClassApiResponse
}