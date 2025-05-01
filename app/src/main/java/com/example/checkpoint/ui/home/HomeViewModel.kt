package com.example.checkpoint.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.BuildConfig
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.data.models.GameResponse
import com.example.checkpoint.data.network.ApiRAWG
import com.example.checkpoint.data.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response // <-- Importar Response de Retrofit

class HomeViewModel : ViewModel() {

    private val _games = MutableLiveData<List<Game>>()
    val games: LiveData<List<Game>> = _games

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var currentPage = 1
    private var isFetching = false
    private var hasMoreGames = true
    private val pageSize = 20 // Define cuántos juegos cargar por página

    // --- Cambio: Renombrar variable a camelCase ---
    private val apiKey = BuildConfig.RAWG_API_KEY

    private val apiService: ApiRAWG = RetrofitClient.instance

    init {
        // --- Cambio: Simplificar condición ---
        if (apiKey.isEmpty()) {
            Log.e("HomeViewModel", "API Key for RAWG is missing or invalid in secrets.properties / BuildConfig.")
            _errorMessage.value = "API Key missing. Please configure secrets.properties."
        } else {
            loadGames()
        }
    }

    fun loadGames() {
        if (isFetching || !hasMoreGames) return

        viewModelScope.launch {
            isFetching = true
            // Mostrar ProgressBar solo al cargar la primera página o si se prefiere siempre
            if (currentPage == 1) {
                _isLoading.value = true
            }
            _errorMessage.value = null // Limpiar errores previos al intentar cargar

            try {
                Log.d("HomeViewModel", "Fetching games page $currentPage with key $apiKey")

                // --- Cambios: Añadir pageSize y manejar Response<T> ---
                val response: Response<GameResponse> = apiService.getGames(apiKey, currentPage, pageSize) // Pasar pageSize

                if (response.isSuccessful) {
                    val gameResponse = response.body() // Obtener el cuerpo GameResponse
                    if (gameResponse != null) {
                        val currentList = _games.value ?: emptyList()
                        _games.value = currentList + gameResponse.results
                        hasMoreGames = true
                    } else {
                        // Respuesta exitosa pero cuerpo nulo (raro para esta API)
                        Log.e("HomeViewModel", "Successful response but empty body")
                        _errorMessage.value = "Error: Received empty response body."
                        hasMoreGames = false // Detener paginación si hay error
                    }
                } else {
                    // Manejar error HTTP (ej. 401 Unauthorized, 404 Not Found, 500 Server Error)
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("HomeViewModel", "API Error: ${response.code()} - $errorBody")
                    _errorMessage.value = "Error ${response.code()}: Could not load games. $errorBody"
                    hasMoreGames = false // Detener paginación si hay error
                }
                // --- Fin Cambios ---

            } catch (e: Exception) {
                // Manejar otros errores (ej. red, parseo JSON)
                Log.e("HomeViewModel", "Error fetching games", e)
                e.printStackTrace()
                _errorMessage.value = "Error loading games: ${e.message}"
                // Considerar no deshabilitar futuras cargas si fue un error temporal
                hasMoreGames = false // Detener paginación si hay error
            } finally {
                _isLoading.value = false // Ocultar ProgressBar
                isFetching = false
            }
        }
    }

    fun loadMoreGames() {
        Log.d("HomeViewModel", "loadMoreGames called. isFetching: $isFetching, hasMoreGames: $hasMoreGames")
        // Cargar más solo si no se está cargando ya y si la API indicó que hay más páginas
        if (!isFetching && hasMoreGames) {
            loadGames()
        }
    }
}
// --- End of HomeViewModel.kt ---