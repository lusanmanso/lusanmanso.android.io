package com.example.checkpoint.data.repository

import com.example.checkpoint.data.models.Game
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

public class FavoritesRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserFavoritesCollection() = auth.currentUser?.uid?.let { userId ->
        firestore.collection("users").document(userId).collection("favorites")
    }

    public suspend fun addFavorite(game: Game) {
        if (game.id <= 0) throw IllegalArgumentException("Invalid game ID")

        val collection = getUserFavoritesCollection()
            ?: throw IllegalStateException("User not logged in")
        try {
            collection.document(game.id.toString()).set(game).await()
        } catch (e: Exception) {
            throw RuntimeException("Error adding favorite: ${e.message}", e)
        }
    }

    public suspend fun removeFavorite(gameId: String) {
        val collection = getUserFavoritesCollection()
            ?: throw IllegalStateException("User not logged in")
        try {
            collection.document(gameId).delete().await()
        } catch (e: Exception) {
            throw RuntimeException("Error removing favorite: ${e.message}", e)
        }
    }

    public suspend fun getFavorites(): List<Game> {
        val collection = getUserFavoritesCollection()
            ?: return emptyList()
        return try {
            val querySnapshot  = collection.get().await()
            querySnapshot.toObjects(Game::class.java)
        } catch (e: Exception) {
            println("Error fetching favorites: ${e.message}")
            emptyList()
        }
    }

    public suspend fun isFavorite(gameId: String): Boolean {
        val collection = getUserFavoritesCollection()
            ?: return false
        return try {
            val documentSnapshot = collection.document(gameId).get().await()
            documentSnapshot.exists()
        } catch (e: Exception) {
            println("Error checking favorite status: ${e.message}")
            false
        }
    }
}