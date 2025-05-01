package com.example.checkpoint.data.models

// Overall struct of the endpoint response
class GameResponse(
    val next: String, // Url to the next page
    val results: List<Game>
)