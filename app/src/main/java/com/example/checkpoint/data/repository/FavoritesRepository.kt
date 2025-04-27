package com.example.checkpoint.data.repository

import com.example.checkpoint.data.models.Game
import com.example.checkpoint.data.network.ApiRAWG
import com.example.checkpoint.data.network.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoritesRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val apiService = RetrofitClient.instance

    suspend fun addToFavorites(userId: String, game: Game) = withContext(Dispatchers.IO) {
        val favoriteRef = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(game.id.toString())

        // Store Game object directly (Firestore supports mapping data classes)
        // Add timestamp for potential sorting later if needed
        val data = game.copyForFirestore() // Assumes a helper function or manual mapping
        favoriteRef.set(data).await()
    }

    // Helper extension function in the same file or a utils file
    // To handle potential nulls Firestore doesn't like directly if Game class has defaults
    // Or just map manually in addToFavorites
    private fun Game.copyForFirestore(): Map<String, Any?> {
        return mapOf(
            "id" to this.id,
            "name" to this.name,
            "background_image" to this.backgroundImage,
            "metacritic" to this.metacritic,
            "timestamp" to System.currentTimeMillis() // Add timestamp here
        )
    }

    /**
     * Elimina un juego de los favoritos del usuario
     */
    suspend fun removeFromFavorites(userId: String, gameId: Int) = withContext(Dispatchers.IO) {
        val favoriteRef = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(gameId.toString())

        favoriteRef.delete().await()
    }

    // Check if the game is in the user's favorites
    suspend fun isFavorite(userId: String, gameId: Int): Boolean = withContext(Dispatchers.IO) {
        val favoriteRef = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(gameId.toString())

        val document = favoriteRef.get().await()
        document.exists()
    }

    suspend fun getFavoriteGames(userId: String): List<Game> = withContext(Dispatchers.IO) {
        val favoritesCollection = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            // Optional: Order by timestamp if you added it
            // .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        // Map Firestore documents directly to Game objects
        val games = favoritesCollection.documents.mapNotNull { doc ->
            // Firestore's toObject function automatically maps fields to data class properties
            try {
                doc.toObject(Game::class.java)
            } catch(e: Exception) {
                // Log error or handle missing/malformed data
                // Log.e("FavoritesRepository", "Error converting favorite document ${doc.id}", e)
                null // Skip games that fail to convert
            }
        }
        games
    }
}
