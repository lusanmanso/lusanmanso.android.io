package com.example.checkpoint.data.models

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

data class Game(
    val id: Int = 0,
    val name: String = "",

    @SerializedName("background_image") // For Gson (RAWG API)
    @get:PropertyName("background_image") // For Firestore getter
    @set:PropertyName("background_image") // For Firestore setter
    var backgroundImage: String? = null, // API/Firestore field is "background_image"

    val rating: Double? = null, // From RAWG API
    val released: String? = null, // From RAWG API, nullable date string
    val metacritic: Int? = null, // From RAWG API, nullable score

    // This state is managed locally in the app, not directly mapped from API/Firestore by default.
    // Needs manual handling if you want to sync favorite status with Firestore.
    var isFavorite: Boolean = false
) {
    // The default values in the primary constructor provide the needed no-argument constructor for Firestore.
}