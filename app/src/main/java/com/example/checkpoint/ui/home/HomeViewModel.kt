package com.example.checkpoint.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.data.network.RetrofitClient
import kotlinx.coroutines.launch
import com.example.checkpoint.BuildConfig

class HomeViewModel: ViewModel() {

    // LiveData for the list of games
    private val _games = MutableLiveData<List<Game>>()
    val games: LiveData<List<Game>> get() = _games

    // LiveData for the loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for error mssg
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Initialize
    fun loadGames(page: Int = 1, pageSize: Int = 20, search: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""

            try {
                // Llamada a la API usando el cliente Retrofit y la interfaz del servicio
                val response = RetrofitClient.instance.getGames(
                    apiKey = BuildConfig.RAWG_API_KEY, // ¡Usa la clave desde BuildConfig!
                    page = page,
                    pageSize = pageSize,
                    search = search
                )

                if (response.isSuccessful) {
                    // Success: Update el LiveData with the list
                    _games.value = response.body()?.results ?: emptyList()
                    Log.d("HomeViewModel", "Juegos cargados: ${response.body()?.results?.size ?: 0}")
                } else {
                    // Error: Notify HTTP e
                    _errorMessage.value = "Error ${response.code()}: ${response.message()}"
                    Log.e("HomeViewModel", "Error de API: ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Exception
                _errorMessage.value = "Error de conexión: ${e.message}"
                Log.e("HomeViewModel", "Excepción al cargar juegos", e)
            } finally {
                _isLoading.value = false // Indicate load final
            }
        }
    }
}