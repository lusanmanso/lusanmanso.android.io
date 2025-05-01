package com.example.checkpoint.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.data.repository.FavoritesRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

public class FavoritesViewModel : ViewModel() {

    private val favoritesRepository = FavoritesRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _favoriteGames = MutableLiveData<List<Game>>()
    public val favoriteGames: LiveData<List<Game>> get() = _favoriteGames

    private val _isLoading = MutableLiveData<Boolean>()
    public val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    public val error: LiveData<String?> get() = _error

    public fun loadFavoriteGames() {
        val currentUser = auth.currentUser ?: run {
            _error.value = "Unidentified user"
            _favoriteGames.value = emptyList()
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val favorites = favoritesRepository.getFavorites()
                _favoriteGames.value = favorites
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
                _favoriteGames.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    public fun addFavorite(game: Game) {
        val currentUser = auth.currentUser ?: run {
            _error.value = "Login required to add favorite"
            return
        }
        viewModelScope.launch {
            try {

                favoritesRepository.addFavorite(game)
                loadFavoriteGames()
            } catch (e: Exception) {
                _error.postValue("Failed to add favorite: ${e.message}")
            }
        }
    }

    public fun removeFromFavorites(gameId: Int) {
        val currentUser = auth.currentUser ?: run {
            _error.value = "Login required to remove from favorites"
            return
        }

        viewModelScope.launch {
            try {
                favoritesRepository.removeFavorite(gameId.toString())
                loadFavoriteGames()
            } catch (e: Exception) {
                _error.postValue("Failed to remove favorite: ${e.message}")            }
        }
    }

    public fun clearError() {
        _error.value = null
    }
}