package com.ludovish.app.core

import android.content.Context
import com.ludovish.app.domain.CareerStats
import com.ludovish.app.domain.SavedGameRecord
import org.json.JSONArray
import org.json.JSONObject

class CareerManager(context: Context) {
    private val prefs = context.getSharedPreferences("ludovish_offline_vault_v2", Context.MODE_PRIVATE)

    fun loadStats(): CareerStats {
        return CareerStats(
            playerName = prefs.getString("player_name", "TrollDrako") ?: "TrollDrako",
            statusText = prefs.getString("status_text", "Ludo Grandmaster") ?: "Ludo Grandmaster",
            country = prefs.getString("country", "🇮🇳 India") ?: "🇮🇳 India",
            avatarIndex = prefs.getInt("avatar_index", 0),
            coins = prefs.getInt("coins", 2650),
            gems = prefs.getInt("gems", 150),
            xp = prefs.getInt("xp", 40),
            gamesWon = prefs.getInt("games_won", 0),
            gamesLost = prefs.getInt("games_lost", 0),
            winStreak = prefs.getInt("win_streak", 0),
            tokensCaptured = prefs.getInt("tokens_captured", 0),
            tokensKilled = prefs.getInt("tokens_killed", 0),
            tournamentsWon = prefs.getInt("tournaments_won", 0)
        )
    }

    fun saveStats(stats: CareerStats) {
        prefs.edit()
            .putString("player_name", stats.playerName)
            .putString("status_text", stats.statusText)
            .putString("country", stats.country)
            .putInt("avatar_index", stats.avatarIndex)
            .putInt("coins", stats.coins)
            .putInt("gems", stats.gems)
            .putInt("xp", stats.xp)
            .putInt("games_won", stats.gamesWon)
            .putInt("games_lost", stats.gamesLost)
            .putInt("win_streak", stats.winStreak)
            .putInt("tokens_captured", stats.tokensCaptured)
            .putInt("tokens_killed", stats.tokensKilled)
            .putInt("tournaments_won", stats.tournamentsWon)
            .apply()
    }

    fun loadSavedGames(): List<SavedGameRecord> {
        val raw = prefs.getString("saved_games_json", null) ?: return defaultSampleSavedGames()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val namesArr = obj.getJSONArray("playerNames")
                val colorsArr = obj.getJSONArray("playerColorIndices")
                val botsArr = obj.getJSONArray("playerBots")
                val progArr = obj.getJSONArray("tokenProgressFlat")

                SavedGameRecord(
                    id = obj.getString("id"),
                    timestamp = obj.getString("timestamp"),
                    playerCount = obj.getInt("playerCount"),
                    gameModeName = obj.getString("gameModeName"),
                    oneTokenOut = obj.optBoolean("oneTokenOut", false),
                    friendlyKill = obj.optBoolean("friendlyKill", false),
                    currentTurnIndex = obj.getInt("currentTurnIndex"),
                    playerNames = List(namesArr.length()) { namesArr.getString(it) },
                    playerColorIndices = List(colorsArr.length()) { colorsArr.getInt(it) },
                    playerBots = List(botsArr.length()) { botsArr.getBoolean(it) },
                    tokenProgressFlat = List(progArr.length()) { progArr.getInt(it) }
                )
            }
        }.getOrElse { defaultSampleSavedGames() }
    }

    fun saveGamesList(list: List<SavedGameRecord>) {
        runCatching {
            val arr = JSONArray()
            list.forEach { rec ->
                val obj = JSONObject().apply {
                    put("id", rec.id)
                    put("timestamp", rec.timestamp)
                    put("playerCount", rec.playerCount)
                    put("gameModeName", rec.gameModeName)
                    put("oneTokenOut", rec.oneTokenOut)
                    put("friendlyKill", rec.friendlyKill)
                    put("currentTurnIndex", rec.currentTurnIndex)
                    put("playerNames", JSONArray(rec.playerNames))
                    put("playerColorIndices", JSONArray(rec.playerColorIndices))
                    put("playerBots", JSONArray(rec.playerBots))
                    put("tokenProgressFlat", JSONArray(rec.tokenProgressFlat))
                }
                arr.put(obj)
            }
            prefs.edit().putString("saved_games_json", arr.toString()).apply()
        }
    }

    private fun defaultSampleSavedGames(): List<SavedGameRecord> = listOf(
        SavedGameRecord(
            id = "sample_1",
            timestamp = "Tue, 18 Aug 2026 11:38:44",
            playerCount = 2,
            gameModeName = "CLASSIC",
            oneTokenOut = false,
            friendlyKill = false,
            currentTurnIndex = 0,
            playerNames = listOf("Player 1", "Player 2"),
            playerColorIndices = listOf(1, 2),
            playerBots = listOf(false, false),
            tokenProgressFlat = listOf(0, -1, -1, -1, 8, -1, -1, -1)
        ),
        SavedGameRecord(
            id = "sample_2",
            timestamp = "Wed, 24 Jun 2026 19:34:12",
            playerCount = 4,
            gameModeName = "CLASSIC",
            oneTokenOut = true,
            friendlyKill = false,
            currentTurnIndex = 0,
            playerNames = listOf("Player 1", "Player 2", "Player 3", "Player 4"),
            playerColorIndices = listOf(0, 1, 2, 3),
            playerBots = listOf(false, false, false, false),
            tokenProgressFlat = List(16) { if (it % 4 == 0) 0 else -1 }
        )
    )
}
