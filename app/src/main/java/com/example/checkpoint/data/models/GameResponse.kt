package com.example.checkpoint.data.models

// Overall struct of the endpoint response
public class GameResponse(
    public val next: String, // Url to the next page
    public val results: List<Game>
)