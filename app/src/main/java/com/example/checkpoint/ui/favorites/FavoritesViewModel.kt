package com.example.checkpoint.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.data.repository.FavoritesRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {

    private val favoritesRepository = FavoritesRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _favoriteGames = MutableLiveData<List<Game>>()
    val favoriteGames: LiveData<List<Game>> get() = _favoriteGames

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadFavoriteGames() {
        val currentUser = auth.currentUser ?: run {
            _error.value = "Unidentified user"
            _favoriteGames.value = emptyList()
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val favorites = favoritesRepository.getFavoriteGames(currentUser.uid)
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

    fun removeFromFavorites(gameId: Int) {
        val currentUser = auth.currentUser ?: run {
            _error.value = "Login required to remove from favorites"
            return
        }

        viewModelScope.launch {
            try {
                favoritesRepository.removeFromFavorites(currentUser.uid, gameId)
                loadFavoriteGames() // Update list after deleting
            } catch (e: Exception) {
                _error.postValue("Failed to remove favorite: ${e.message}")            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
