package pl.filked.malin_pozycjonowanie.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.filked.malin_pozycjonowanie.data.dataSources.BeaconDataSource
import pl.filked.malin_pozycjonowanie.data.mappers.toDomain
import pl.filked.malin_pozycjonowanie.domain.model.Beacon

class MainViewModel(
    private val beaconDataSource: BeaconDataSource
) : ViewModel() {

    companion object{
        private const val TAG =  "pw.MainViewModel"
    }

    private val _beacons = MutableStateFlow<List<Beacon>>(emptyList())
    val beacons: StateFlow<List<Beacon>> = _beacons

    init {
        loadBeacons()
    }

    fun loadBeacons() {
        viewModelScope.launch {
            val result = beaconDataSource.loadBeacons()
            result.onSuccess { beaconsDto ->
                _beacons.value = beaconsDto.map { it.toDomain() }
            }.onFailure { error ->
                Log.e(TAG, "Failed to load beacons", error)
            }
        }
    }
}