package com.juhyeonyu.isitgood.data.repository

import com.juhyeonyu.isitgood.data.model.Game
import com.juhyeonyu.isitgood.data.remote.RetrofitClient

object GameRepository {

    private val gameCache = mutableMapOf<Int, Game>()
    private val summaryCache = mutableMapOf<Int, String>()

    suspend fun searchGames(query: String): List<Game> {
        val results = RetrofitClient.api.searchGames(query)
        results.forEach { game -> gameCache[game.id] = game }
        return results
    }

    fun getGame(rawgId: Int): Game? {
        return gameCache[rawgId]
    }

    fun cacheSummary(rawgId: Int, summary: String) {
        summaryCache[rawgId] = summary
    }

    fun getSummary(rawgId: Int): String? {
        return summaryCache[rawgId]
    }
}