package pl.filked.malin_pozycjonowanie.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.filked.malin_pozycjonowanie.data.ResultState
import pl.filked.malin_pozycjonowanie.data.repository.QrRepository
import pl.filked.malin_pozycjonowanie.domain.model.Position

class MainViewModel(
    private val repository: QrRepository
) : ViewModel() {

    private val _scannedText = MutableStateFlow("")
    val scannedText: StateFlow<String> = _scannedText.asStateFlow()

    private val _locationState = MutableStateFlow<ResultState<Position>?>(null)
    val locationState: StateFlow<ResultState<Position>?> = _locationState.asStateFlow()

    fun processQrCode(qrContent: String?) {
        if (qrContent.isNullOrBlank()) return

        val lastPhrase = qrContent.substringAfterLast("/")
        _scannedText.value = lastPhrase

        _locationState.value = ResultState.Loading

        viewModelScope.launch {
            _locationState.value = repository.getPosition(lastPhrase)
        }
    }
}

class MainViewModelFactory(private val repository: QrRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}