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
import retrofit2.Response

public class HomeViewModel : ViewModel() {

    private val _games = MutableLiveData<List<Game>>()
    public val games: LiveData<List<Game>> = _games

    private val _isLoading = MutableLiveData<Boolean>()
    public val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    public val errorMessage: LiveData<String?> = _errorMessage

    private var currentPage = 1
    private var isFetching = false
    private var hasMoreGames = true
    private val pageSize = 20

    private val apiKey = BuildConfig.RAWG_API_KEY

    private val apiService: ApiRAWG = RetrofitClient.instance

    init {
        if (apiKey.isEmpty()) {
            Log.e("HomeViewModel", "API Key for RAWG is missing or invalid in secrets.properties / BuildConfig.")
            _errorMessage.value = "API Key missing. Please configure secrets.properties."
        } else {
            loadGames()
        }
    }

    private fun loadGames() {
        if (isFetching || !hasMoreGames) return

        viewModelScope.launch {
            isFetching = true
            if (currentPage == 1) {
                _isLoading.value = true
            }
            _errorMessage.value = null

            try {
                Log.d("HomeViewModel", "Fetching games page $currentPage with key $apiKey")

                val response: Response<GameResponse> = apiService.getGames(apiKey, currentPage, pageSize)

                if (response.isSuccessful) {
                    val gameResponse = response.body()
                    if (gameResponse != null) {
                        val currentList = _games.value ?: emptyList()
                        _games.value = currentList + gameResponse.results
                        // Increment currentPage ONLY if the response was successful and we got results
                        currentPage++
                        // Assume there are more games if the result list size equals page size, otherwise assume we reached the end
                        hasMoreGames = gameResponse.results.size == pageSize
                    } else {
                        Log.e("HomeViewModel", "Successful response but empty body")
                        _errorMessage.value = "Error: Received empty response body."
                        hasMoreGames = false
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("HomeViewModel", "API Error: ${response.code()} - $errorBody")
                    _errorMessage.value = "Error ${response.code()}: Could not load games. $errorBody"
                    hasMoreGames = false
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching games", e)
                e.printStackTrace()
                _errorMessage.value = "Error loading games: ${e.message}"
                hasMoreGames = false
            } finally {
                _isLoading.value = false
                isFetching = false
            }
        }
    }

    public fun loadMoreGames() {
        Log.d("HomeViewModel", "loadMoreGames called. isFetching: $isFetching, hasMoreGames: $hasMoreGames")
        if (!isFetching && hasMoreGames) {
            // currentPage is incremented inside loadGames upon successful fetch
            loadGames()
        }
    }

    // Added refresh function
    public fun refreshGames() {
        // Reset state
        currentPage = 1
        hasMoreGames = true
        isFetching = false
        _games.value = emptyList() // Clear existing games immediately

        // Load first page
        loadGames()
    }
}