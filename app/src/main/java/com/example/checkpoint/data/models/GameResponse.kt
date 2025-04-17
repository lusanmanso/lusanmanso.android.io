package com.example.checkpoint.data.models

// Overall struct of the endpoint response
class GameResponse(
    val count: Int,
    val next: String, // Url to the next page
    val previous: String, // Url to the previous page
    val results: List<Game>
)