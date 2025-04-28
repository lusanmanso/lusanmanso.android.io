package com.example.checkpoint.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.models.Game
import com.example.checkpoint.data.network.RetrofitClient
import com.example.checkpoint.data.repository.FavoritesRepository // Assuming you have this
import com.google.firebase.auth.FirebaseAuth // Needed for user ID
import kotlinx.coroutines.launch
import com.example.checkpoint.BuildConfig

class HomeViewModel: ViewModel() { // Consider injecting repository: class HomeViewModel(private val favoritesRepository: FavoritesRepository): ViewModel()

    // LiveData for the games list
    private val _games = MutableLiveData<List<Game>>()
    val games: LiveData<List<Game>> get() = _games

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for error message
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // LiveData for pagination loading state
    private val _isLoadingMore = MutableLiveData<Boolean>(false) // Initialize to false
    val isLoadingMore: LiveData<Boolean> get() = _isLoadingMore

    // Pagination control
    private var currentPage = 1
    private var isLastPage = false
    private var isLoadingMoreInternal = false // Renamed to avoid conflict
    private var currentQuery: String? = null
    private var currentGenre: String? = null

    // Assuming FavoritesRepository is available (e.g., injected or instantiated)
    // This is a simplified example; proper dependency injection is recommended.
    private val favoritesRepository = FavoritesRepository()
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    // Initialize
    fun loadGames(page: Int = 1, pageSize: Int = 20, search: String? = null, genre: String? = null, resetList: Boolean = true) {
        // If we're requesting the first page or changing filters, reset the state
        if (page == 1 || search != currentQuery || genre != currentGenre) {
            currentPage = 1
            isLastPage = false
            currentQuery = search
            currentGenre = genre
        }

        // If we're already loading or reached the last page, do nothing
        // Corrected: Access .value for LiveData
        if ((isLoading.value == true || isLoadingMore.value == true) || (page > 1 && isLastPage)) {
            return
        }

        // Decide if we're loading from scratch or paginating
        val isLoadingNewPage = page > 1

        if (isLoadingNewPage) {
            _isLoadingMore.value = true
        } else {
            _isLoading.value = true
        }

        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                // API call using Retrofit client
                val response = RetrofitClient.instance.getGames(
                    apiKey = BuildConfig.RAWG_API_KEY,
                    page = page,
                    pageSize = pageSize,
                    search = search,
                    genres = genre
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val newGames = responseBody?.results ?: emptyList()

                    // Check if we've reached the last page
                    isLastPage = responseBody?.next == null || newGames.isEmpty()

                    // Update the current page
                    if (!isLastPage) {
                        currentPage = page + 1
                    }

                    // Update the games list
                    if (resetList || _games.value == null) {
                        _games.value = newGames
                    } else {
                        // Add to the existing list
                        val currentGames = _games.value?.toMutableList() ?: mutableListOf()
                        currentGames.addAll(newGames)
                        _games.value = currentGames
                    }

                    Log.d("HomeViewModel", "Games loaded (page $page): ${newGames.size}")
                } else {
                    // Error: Notify HTTP error
                    _errorMessage.value = "Error ${response.code()}: ${response.message()}"
                    Log.e("HomeViewModel", "API Error: ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Exception
                _errorMessage.value = "Connection error: ${e.message}"
                Log.e("HomeViewModel", "Exception while loading games", e)
            } finally {
                if (isLoadingNewPage) {
                    _isLoadingMore.value = false
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    // Method to load the next page
    fun loadNextPage() {
        // Corrected: Access .value for LiveData and use renamed internal variable
        if (!isLastPage && isLoadingMore.value != true) {
            loadGames(page = currentPage, search = currentQuery, genre = currentGenre, resetList = false)
        }
    }

    // Method to refresh the list (resets pagination)
    fun refreshGames() {
        loadGames(search = currentQuery, genre = currentGenre)
    }

    // Function to toggle favorite status
    fun toggleFavorite(game: Game) {
        userId?.let { uid ->
            viewModelScope.launch {
                try {
                    val isCurrentlyFavorite = favoritesRepository.isFavorite(game.id.toString()) // Check current status
                    if (isCurrentlyFavorite) {
                        favoritesRepository.removeFavorite(game.id.toString())
                        Log.d("HomeViewModel", "Removed favorite: ${game.id}")
                        // Optionally update game item in _games LiveData or notify adapter
                    } else {
                        favoritesRepository.addFavorite(game)
                        Log.d("HomeViewModel", "Added favorite: ${game.id}")
                        // Optionally update game item in _games LiveData or notify adapter
                    }
                    // Simplest approach: Refresh the specific item in the adapter if possible,
                    // or trigger a subtle UI update. A full refresh might be too much.
                    // For now, we rely on the adapter updating itself if it observes favorite changes.
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error toggling favorite for game ${game.id}", e)
                    _errorMessage.postValue("Failed to update favorite status.")
                }
            }
        } ?: run {
            _errorMessage.postValue("You must be logged in to manage favorites.")
        }
    }
}
