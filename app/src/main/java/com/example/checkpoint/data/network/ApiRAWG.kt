package com.example.checkpoint.data.network

import com.example.checkpoint.data.models.GameResponse
import com.example.checkpoint.data.models.GameDetail
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiRAWG {
    @GET("games")
    suspend fun getGames(
        @Query("key") apiKey: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        // @Query("search") search: String? = null,
        // TODO: Add more queries for filtering
    ): Response<GameResponse>

    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") gameId: Int,
        @Query("key") apiKey: String
    ): Response<GameDetail>

}