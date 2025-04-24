package com.example.checkpoint.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.BuildConfig
import com.example.checkpoint.data.models.GameDetail
import com.example.checkpoint.data.network.ApiRAWG
import com.example.checkpoint.data.network.RetrofitClient
import kotlinx.coroutines.launch

class GameDetailViewModel : ViewModel() {

    // Instancia del servicio Retrofit
    private val apiService: ApiRAWG = RetrofitClient.instance

    // LiveData para exponer los detalles del juego
    private val _gameDetails = MutableLiveData<GameDetail?>()
    val gameDetails: LiveData<GameDetail?> = _gameDetails

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para errores
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Función para cargar los detalles del juego
    fun loadGameDetails(gameId: Int) {
        if (_gameDetails.value?.id == gameId) return // Evitar recargar si ya tenemos los datos

        _isLoading.value = true
        _error.value = null // Limpiar error anterior
        viewModelScope.launch {
            try {
                // Asegúrate de obtener tu API_KEY de forma segura (BuildConfig o similar)
                val apiKey = BuildConfig.RAWG_API_KEY // Asume que la tienes en build.gradle (ver nota abajo)

                val response = apiService.getGameDetails(gameId, apiKey)
                if (response.isSuccessful) {
                    _gameDetails.postValue(response.body())
                } else {
                    _error.postValue("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                // Manejo de excepciones (red, parsing, etc.)
                _error.postValue("Error al cargar detalles: ${e.localizedMessage ?: e.message}")
                e.printStackTrace() // Loguea el error completo para depuración
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Función para limpiar el mensaje de error una vez mostrado
    fun clearError() {
        _error.value = null
    }
}