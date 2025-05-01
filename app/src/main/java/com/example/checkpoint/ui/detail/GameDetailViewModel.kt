package com.example.checkpoint.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.BuildConfig
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.data.models.GameDetail
import com.example.checkpoint.data.network.RetrofitClient
import com.example.checkpoint.data.repository.FavoritesRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception

public class GameDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _isLoading = MutableLiveData<Boolean>()
    public val isLoading: LiveData<Boolean> get() = _isLoading

    private val _gameDetails = MutableLiveData<GameDetail?>()
    public val gameDetails: MutableLiveData<GameDetail?> get() = _gameDetails

    private val _error = MutableLiveData<String?>()
    public val error: LiveData<String?> get() = _error

    private val _isFavorite = MutableLiveData<Boolean>()
    public val isFavorite: LiveData<Boolean> get() = _isFavorite

    private val favoritesRepository = FavoritesRepository()
    private val auth = FirebaseAuth.getInstance()

    public fun loadGameDetails(gameId: Int) {
        _isLoading.value = true
        _error.value = null
        val userId = auth.currentUser?.uid

        if (userId != null) {
            viewModelScope.launch {
                try {
                    val favoriteStatus = favoritesRepository.isFavorite(gameId.toString())
                    _isFavorite.postValue(favoriteStatus)
                } catch (e: Exception) {
                    _isFavorite.postValue(false)

                }
            }
        } else {
            _isFavorite.value = false
        }

        viewModelScope.launch {
            try {
                val gameDetailResponse: Response<GameDetail> = RetrofitClient.instance.getGameDetails(
                    gameId = gameId,
                    apiKey = BuildConfig.RAWG_API_KEY
                )

                if (gameDetailResponse.isSuccessful) {

                    val detail: GameDetail? = gameDetailResponse.body()
                    if (detail != null) {
                        _gameDetails.postValue(detail)
                    } else {
                        _error.postValue("Response body was empty.")
                    }
                } else {
                    _error.postValue("Error ${gameDetailResponse.code()}: ${gameDetailResponse.message()}")
                }

            } catch (e: Exception) {
                _error.postValue(e.message ?: "Failed to load game details")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    public fun toggleFavorite() {
        val userId = auth.currentUser?.uid ?: run {
            _error.postValue("You must be logged in to manage favorites.")
            return
        }
        val detail = _gameDetails.value ?: run {
            _error.postValue("Game details not loaded yet.")
            return
        }
        val currentlyFavorite = _isFavorite.value ?: false

        val game = Game(
            id = detail.id,
            name = detail.name,
            backgroundImage = detail.backgroundImage,
            rating = detail.rating,
            released = detail.released,
            metacritic = detail.metacritic
        )

        viewModelScope.launch {
            try {
                if (currentlyFavorite) {
                    favoritesRepository.removeFavorite(game.id.toString())
                    _isFavorite.postValue(false)
                } else {
                    favoritesRepository.addFavorite(game)
                    _isFavorite.postValue(true)
                }
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue("Failed to update favorites: ${e.message}")
            }
        }
    }

    public fun clearError() {
        _error.value = null
    }
}