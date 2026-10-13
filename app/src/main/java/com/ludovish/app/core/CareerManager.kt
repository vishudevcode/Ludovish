package com.ludovish.app.core

import android.content.Context
import com.ludovish.app.domain.CareerStats

class CareerManager(context: Context) {
    private val prefs = context.getSharedPreferences("ludovish_offline_vault", Context.MODE_PRIVATE)

    fun loadStats(): CareerStats {
        return CareerStats(
            coins = prefs.getInt("coins", 5000),
            xp = prefs.getInt("xp", 120),
            matchesPlayed = prefs.getInt("matches_played", 0),
            matchesWon = prefs.getInt("matches_won", 0),
            tokensCaptured = prefs.getInt("tokens_captured", 0),
            sixesRolled = prefs.getInt("sixes_rolled", 0)
        )
    }

    fun saveStats(stats: CareerStats) {
        prefs.edit()
            .putInt("coins", stats.coins)
            .putInt("xp", stats.xp)
            .putInt("matches_played", stats.matchesPlayed)
            .putInt("matches_won", stats.matchesWon)
            .putInt("tokens_captured", stats.tokensCaptured)
            .putInt("sixes_rolled", stats.sixesRolled)
            .apply()
    }
}
