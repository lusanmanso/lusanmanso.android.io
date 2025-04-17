package com.example.checkpoint.data.network

import com.example.checkpoint.data.models.GameResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiRAWG {
    @GET("games")
    suspend fun getGames(
        @Query("key") apiKey: String,
        @Query("page") page: Int? = 1,
        @Query("page_size") pageSize: Int? = 20,
        @Query("search") search: String? = null,
        // TODO: Add more queries for filtering
    ): Response<GameResponse>

}