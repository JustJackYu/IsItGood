package com.juhyeonyu.isitgood.data.remote

import com.juhyeonyu.isitgood.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @GET("games/search")
    suspend fun searchGames(@Query("q") query: String): List<Game>

    @GET("games/{id}")
    suspend fun getGame(@Path("id") id: Int): Game

    @GET("games/saved")
    suspend fun getSavedGames(): List<SavedGame>

    @POST("games/save")
    suspend fun saveGame(@Body request: SaveGameRequest): SavedGame

    @DELETE("games/save/{rawgId}")
    suspend fun unsaveGame(@Path("rawgId") rawgId: Int): MessageResponse

    @GET("games/{id}/summary")
    suspend fun getGameSummary(
        @Path("id") id: Int,
        @Query("name") name: String
    ): GameSummary

    @GET("games/{id}/prices")
    suspend fun getGamePrices(
        @Path("id") id: Int,
        @Query("name") name: String
    ): List<Deal>

    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}