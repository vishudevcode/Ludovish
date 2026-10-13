package com.ludovish.app.domain

import androidx.compose.ui.graphics.Color

enum class GlassMode(val label: String) {
    CLEAR_LIQUID("Clear Liquid"),
    FROSTED_TRANSLUCENT("Frosted Glass"),
    OPAQUE_MATTE("Opaque Matte")
}

enum class GameMode(val title: String, val subtitle: String, val tokensToWin: Int) {
    CLASSIC("Classic", "All 4 tokens home to win", 4),
    QUICK_RUSH("Quick Ludo", "Fast match • 1 token home wins!", 1),
    MASTER("Master Mode", "Capture 1 enemy to unlock Home Stretch", 4),
    TEAM_UP("Team 2v2+", "Allied partners • No friendly fire", 4)
}

enum class TokenStyle(val title: String) {
    CLASSIC_PIN("Classic Pin"),
    LIQUID_ORB("Liquid Orb"),
    ROYAL_CROWN("Crown"),
    CYBER_HEX("Cyber Hex"),
    DIAMOND_GEM("Diamond"),
    STAR_SHIELD("Star Shield")
}

enum class DiceStyle(val title: String) {
    LIQUID_CRYSTAL("Crystal"),
    IMPERIAL_GOLD("Royal Gold"),
    NEON_MATRIX("Cyber Neon"),
    OBSIDIAN_DARK("Obsidian")
}

enum class BotDifficulty(val label: String) {
    EASY("Casual"),
    TACTICAL("Tactical"),
    GRANDMASTER("Grandmaster")
}

data class CareerStats(
    val coins: Int = 5000,
    val xp: Int = 120,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val tokensCaptured: Int = 0,
    val sixesRolled: Int = 0
) {
    val level: Int get() = (xp / 250) + 1
}

data class ThemePalette(
    val id: String,
    val name: String,
    val primaryAccent: Color,
    val secondaryOrb: Color,
    val deepBackground: Color
)

val PresetPalettes = listOf(
    ThemePalette("aurora", "Vision Aurora", Color(0xFF00F5D4), Color(0xFF7B2CBF), Color(0xFF070913)),
    ThemePalette("cyber_rose", "Liquid Rose", Color(0xFFFF2A85), Color(0xFF4CC9F0), Color(0xFF0F0616)),
    ThemePalette("solar_gold", "Imperial Gold", Color(0xFFFFBE0B), Color(0xFFFB5607), Color(0xFF120C06)),
    ThemePalette("emerald_ice", "Alpine Mint", Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF041014)),
    ThemePalette("ultraviolet", "Deep Nebula", Color(0xFFA855F7), Color(0xFFEC4899), Color(0xFF090514)),
    ThemePalette("titanium", "Apple Titanium", Color(0xFF94A3B8), Color(0xFF38BDF8), Color(0xFF0B0F17))
)

val TenPlayerColors = listOf(
    Color(0xFFFF3B5C), // P1: Crimson Red
    Color(0xFF00E575), // P2: Neon Emerald
    Color(0xFFFFD60A), // P3: Cyber Gold
    Color(0xFF0A84FF), // P4: Electric Azure
    Color(0xFFBF5AF2), // P5: Royal Violet
    Color(0xFFFF9F0A), // P6: Tangerine Burst
    Color(0xFF64D2FF), // P7: Glacial Cyan
    Color(0xFFFF375F), // P8: Hot Magenta
    Color(0xFF30D158), // P9: Mint Lime
    Color(0xFF5E5CE6)  // P10: Indigo Pulse
)

val TenPlayerNames = listOf(
    "Red Monarch", "Green Viper", "Gold Phoenix", "Blue Titan", "Violet Nova",
    "Orange Blaze", "Cyan Frost", "Rose Striker", "Lime Phantom", "Indigo Apex"
)

data class Token(
    val id: Int,
    val playerIndex: Int,
    val progress: Int = -1
) {
    val isInBase: Boolean get() = progress == -1
    val isFinished: Boolean get() = progress == 56
    val isInHomeStretch: Boolean get() = progress in 51..55

    fun canMove(diceValue: Int, gameMode: GameMode, playerHasCaptured: Boolean): Boolean {
        if (isFinished) return false
        if (isInBase) return diceValue == 6
        val target = progress + diceValue
        if (target > 56) return false
        // Master Mode Rule: Must have captured at least 1 opponent token to enter Home Stretch (51..56)
        if (gameMode == GameMode.MASTER && target >= 51 && !playerHasCaptured) {
            return false
        }
        return true
    }
}

data class Player(
    val index: Int,
    val name: String,
    val color: Color,
    val isBot: Boolean = false,
    val hasCapturedEnemy: Boolean = false,
    val tokens: List<Token> = List(4) { Token(id = it, playerIndex = index, progress = -1) },
    val rank: Int? = null
) {
    fun hasMetWinCondition(mode: GameMode): Boolean {
        val finishedCount = tokens.count { it.isFinished }
        return finishedCount >= mode.tokensToWin
    }
}

data class TurnSnapshot(
    val players: List<Player>,
    val currentTurnIndex: Int,
    val diceValue: Int,
    val winnersOrder: List<Int>
)

data class GameUiState(
    val playerCount: Int = 4,
    val gameMode: GameMode = GameMode.CLASSIC,
    val tokenStyle: TokenStyle = TokenStyle.CLASSIC_PIN,
    val diceStyle: DiceStyle = DiceStyle.LIQUID_CRYSTAL,
    val botDifficulty: BotDifficulty = BotDifficulty.TACTICAL,
    val players: List<Player> = emptyList(),
    val currentTurnIndex: Int = 0,
    val diceValue: Int = 6,
    val isRolling: Boolean = false,
    val hasRolledThisTurn: Boolean = false,
    val consecutiveSixes: Int = 0,
    val statusMessage: String = "Tap Dice to Roll",
    val winnersOrder: List<Int> = emptyList(),
    val canUndo: Boolean = false,
    val careerStats: CareerStats = CareerStats(),
    val glassValue: Float = 0.42f,
    val selectedPalette: ThemePalette = PresetPalettes.first(),
    val customHue: Float = 175f,
    val useCustomColor: Boolean = false,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.85f,
    val activeSheet: DockSection = DockSection.PLAY
) {
    val activeAccentColor: Color
        get() = if (useCustomColor) Color.hsv(customHue, 0.85f, 1f) else selectedPalette.primaryAccent

    val glassModeLabel: GlassMode
        get() = when {
            glassValue < 0.33f -> GlassMode.CLEAR_LIQUID
            glassValue < 0.72f -> GlassMode.FROSTED_TRANSLUCENT
            else -> GlassMode.OPAQUE_MATTE
        }
}

enum class DockSection {
    PLAY, MODES_PLAYERS, PAWNS_DICE, GLASS_STUDIO, CAREER_AI
}
