package com.juhyeonyu.isitgood.data.model

import com.google.gson.annotations.SerializedName

// Auth
data class AuthRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val username: String)
data class AuthResponse(val token: String)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
data class MeResponse(val id: Int, val email: String, val username: String?)
data class UpdateUsernameRequest(val username: String)

// Games
data class Game(
    val id: Int,
    val name: String,
    @SerializedName("background_image") val backgroundImage: String?,
    val rating: Float,
    val released: String?
)

data class GameSummary(val summary: String, val sources: List<String>)

data class Deal(
    val store: String,
    val price: Double,
    val url: String,
    val discountPercent: Int = 0
)

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

// Preferences — mirrors backend UserPreferences. Used for both GET response and PUT body.
// Extra server fields (id, userId, updatedAt) are ignored by Gson on read.
data class UserPreferences(
    val summaryLength: String = "MEDIUM",
    val tone: String = "BALANCED",
    val lookOutFor: List<String> = emptyList(),
    val allowMatureContent: Boolean = false,
    val fontSize: String = "MEDIUM",
    val dealDisplay: String = "BOTH",
    val saleAlertDiscount: Int? = null,
    val saleAlertPrice: Double? = null
)

// Deals
data class DealGame(
    val rawgId: Int,
    val title: String,
    val coverImage: String?,
    val rating: Float?,
    val discountPercent: Int,
    val price: Double,
    val regularPrice: Double,
    val currency: String,
    val store: String,
    val url: String
)

data class BestDeal(
    val price: Double,
    val regular: Double,
    val cut: Int,
    val currency: String,
    val store: String,
    val url: String
)

data class SavedGameDeal(
    val rawgId: Int,
    val deal: BestDeal?
)