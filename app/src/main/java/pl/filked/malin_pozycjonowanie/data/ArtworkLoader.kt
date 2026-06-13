package pl.filked.malin_pozycjonowanie.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ArtworkLoader {

    fun load(context: Context): List<Artwork> {

        val json = context.assets
            .open("Obrazy.json")
            .bufferedReader()
            .use { it.readText() }

        return Gson().fromJson(
            json,
            object : TypeToken<List<Artwork>>() {}.type
        )
    }
}