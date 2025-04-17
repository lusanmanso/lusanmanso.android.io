package com.example.checkpoint.data.models

import com.google.gson.annotations.SerializedName

data class Game(
    val id: Int,
    val name: String,
    @SerializedName("background_image")
    val backgroundImage: String, // URL of the img
    val rating: Double,
    val released: String,
    // TODO: Prob add more fields with genre
)