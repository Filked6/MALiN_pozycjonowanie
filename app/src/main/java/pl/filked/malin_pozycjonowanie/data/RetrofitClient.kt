package pl.filked.malin_pozycjonowanie.data

import pl.filked.malin_pozycjonowanie.data.repository.QrRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://arcgis.cenagis.edu.pl/"

    val api: LocationInterface by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationInterface::class.java)
    }

    val qrRepository: QrRepository by lazy {
        QrRepository(api)
    }
}