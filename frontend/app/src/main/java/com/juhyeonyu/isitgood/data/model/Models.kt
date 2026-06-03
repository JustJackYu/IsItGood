package com.juhyeonyu.isitgood.data.model

import com.google.gson.annotations.SerializedName

// Auth
data class AuthRequest(val email: String, val password: String)
data class AuthResponse(val token: String)

// Games
data class Game(
    val id: Int,
    val name: String,
    @SerializedName("background_image") val backgroundImage: String?,
    val rating: Float,
    val released: String?
)

data class GameSummary(val summary: String, val sources: List<String>)

data class Deal(val store: String, val price: Double, val url: String)

// Saved games
data class SavedGame(
    val id: Int,
    val rawgId: Int,
    val title: String,
    val coverImage: String?,
    val rating: Float?,
    val released: String?
)
data class SaveGameRequest(
    val rawgId: Int,
    val title: String,
    val coverImage: String?,
    val rating: Float?,
    val released: String?
)

// Chat
data class ChatMessage(val role: String, val content: String)
data class ChatRequest(
    val message: String,
    val gameTitle: String,
    val summary: String,
    val history: List<ChatMessage>
)
data class ChatResponse(val reply: String)

// Generic
data class MessageResponse(val message: String)