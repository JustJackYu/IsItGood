package com.juhyeonyu.isitgood.data.remote

import com.juhyeonyu.isitgood.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @GET("auth/me")
    suspend fun getMe(): MeResponse

    @PUT("auth/username")
    suspend fun updateUsername(@Body request: UpdateUsernameRequest): MeResponse

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): MessageResponse

    @GET("games/search")
    suspend fun searchGames(@Query("q") query: String): List<Game>

    @GET("games/{id}")
    suspend fun getGame(@Path("id") id: Int): Game

    @GET("games/saved")
    suspend fun getSavedGames(): List<SavedGame>

    @GET("games/saved/deals")
    suspend fun getSavedGameDeals(): List<SavedGameDeal>

    @GET("games/deals")
    suspend fun getDeals(): List<DealGame>

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

    @GET("preferences")
    suspend fun getPreferences(): UserPreferences

    @PUT("preferences")
    suspend fun updatePreferences(@Body request: UserPreferences): UserPreferences
}