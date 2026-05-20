package pl.filked.malin_pozycjonowanie.ui

import android.app.Application
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import pl.filked.malin_pozycjonowanie.R

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ArcGISEnvironment.apiKey = ApiKey.create(getString(R.string.maps_api_key))
    }
}