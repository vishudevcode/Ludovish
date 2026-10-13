package com.ludovish.app.domain

import androidx.compose.ui.graphics.Color

enum class AppScreen {
    SPLASH,
    LUDO_HOME,
    SNAKES_HOME,
    LUDO_GAMEPLAY,
    SNAKES_THEME_SELECT,
    SNAKES_BOARD_SELECT,
    SNAKES_GAMEPLAY
}

enum class BottomNavTab(val label: String) {
    HOME("HOME"),
    EVENT("THEMES"),
    ADDA("SAVED"),
    INVENTORY("INVENTORY"),
    SOCIAL("STATS")
}

enum class ActiveModal {
    NONE,
    SETTINGS,
    STATISTICS,
    EDIT_PROFILE,
    LUDO_MODE_SELECT,
    LUDO_PLAYER_SETUP,
    KEEP_ONE_TOKEN_OUT_PROMPT,
    LOAD_SAVED_GAMES,
    IN_GAME_QUIT,
    IN_GAME_SAVE_PROMPT,
    THEME_COLOR_WHEEL,
    ACCENT_COLOR_WHEEL,
    DAILY_SPIN_WHEEL,
    TUTORIAL_RULES
}

enum class AppThemeMode(val label: String) {
    DARK("Dark Mode"),
    LIGHT("Light Mode"),
    SYSTEM("System Mode")
}

enum class GlassMode(val label: String) {
    CLEAR_LIQUID("Clear Liquid"),
    FROSTED_TRANSLUCENT("Frosted Glass"),
    OPAQUE_MATTE("Opaque Matte")
}

enum class HubEntryType {
    COMPUTER,
    PASS_N_PLAY,
    TEAM_UP,
    TOURNAMENT
}

enum class GameMode(val title: String, val subtitle: String, val tokensToWin: Int) {
    CLASSIC("Classic", "All 4 tokens home to win", 4),
    TEAM_UP("Classic Team Up", "2v2+ Allied Partners", 4),
    QUICK("QUICK", "Fast match • 1 token home wins!", 1),
    RUSH_MODE("Rush Mode", "Simultaneous real-time dice rolls!", 4)
}

enum class TokenStyle(val title: String, val badgeSymbol: String) {
    LOCATION_PIN("Location Pin", "📍"),
    CLASSIC_DISC("Classic Star", "⭐"),
    TROPHY_CUP("Trophy", "🏆"),
    LIGHTNING_BOLT("Bolt", "⚡"),
    TORNADO_SPIN("Tornado", "🌪️"),
    MASKED_NINJA("Ninja", "🥷")
}

enum class LudoBoardTheme(val title: String, val bgPrimary: Color, val bgSecondary: Color) {
    CLASSIC_ROYAL("Royal Blue", Color(0xFF0A3D7A), Color(0xFF051C3B)),
    NATURE_FOREST("Nature", Color(0xFF1B4332), Color(0xFF081C15)),
    EGYPT_SAND("Egypt", Color(0xFF6B4423), Color(0xFF2D1B0E)),
    DISCO_NEON("Disco", Color(0xFF3C096C), Color(0xFF10002B)),
    CANDY_LAND("Candy", Color(0xFF800F2F), Color(0xFF390016)),
    PENGUIN_ICE("Penguin Ice", Color(0xFF005F73), Color(0xFF001219))
}

enum class SnakesTheme(
    val title: String,
    val boardBg: Color,
    val altTile: Color,
    val snakeColor: Color,
    val ladderColor: Color
) {
    DEFAULT("Default", Color(0xFFFAD2E1), Color(0xFFFFFFFF), Color(0xFF2DC653), Color(0xFF0096C7)),
    NATURE("Nature", Color(0xFF95D5B2), Color(0xFFD8F3DC), Color(0xFFD90429), Color(0xFF7F4F24)),
    DISCO("Disco", Color(0xFF240046), Color(0xFF3C096C), Color(0xFF00F5D4), Color(0xFFFF007F)),
    MARBLE("Marble", Color(0xFFDDB892), Color(0xFFEDE0D4), Color(0xFF386641), Color(0xFF7F5539)),
    PENGUIN("Penguin", Color(0xFF90E0EF), Color(0xFFCAF0F8), Color(0xFF03045E), Color(0xFF0077B6)),
    BATTLE("Battle", Color(0xFF606C38), Color(0xFFADC178), Color(0xFFBC4749), Color(0xFF283618)),
    CANDY("Candy", Color(0xFFFFC8DD), Color(0xFFFFAFCC), Color(0xFF7209B7), Color(0xFF3A86FF)),
    EGYPT("Egypt", Color(0xFFE9C46A), Color(0xFFF4A261), Color(0xFF9B2226), Color(0xFF264653))
}

data class SnakesBoardLayout(
    val boardNumber: Int,
    val ladders: Map<Int, Int>,
    val snakes: Map<Int, Int>
)

val SevenSnakesBoards: List<SnakesBoardLayout> = listOf(
    SnakesBoardLayout(1, mapOf(4 to 25, 13 to 46, 33 to 49, 42 to 63, 50 to 69, 62 to 81, 74 to 92), mapOf(27 to 5, 40 to 3, 43 to 18, 54 to 31, 66 to 45, 76 to 58, 89 to 53, 99 to 41)),
    SnakesBoardLayout(2, mapOf(2 to 38, 7 to 14, 8 to 31, 15 to 26, 21 to 42, 28 to 84, 36 to 44, 51 to 67, 71 to 91, 78 to 98), mapOf(16 to 6, 46 to 25, 49 to 11, 62 to 19, 64 to 60, 74 to 53, 89 to 68, 92 to 88, 95 to 75, 99 to 80)),
    SnakesBoardLayout(3, mapOf(3 to 22, 5 to 8, 11 to 26, 20 to 29, 27 to 56, 36 to 57, 51 to 72, 71 to 92, 80 to 99), mapOf(17 to 4, 19 to 7, 21 to 9, 43 to 34, 54 to 31, 62 to 18, 64 to 60, 87 to 24, 93 to 73, 96 to 75)),
    SnakesBoardLayout(4, mapOf(6 to 27, 14 to 35, 24 to 45, 34 to 55, 44 to 65, 56 to 77, 66 to 87, 76 to 97), mapOf(23 to 2, 32 to 11, 48 to 29, 59 to 38, 68 to 47, 79 to 58, 88 to 67, 98 to 12)),
    SnakesBoardLayout(5, mapOf(9 to 31, 18 to 42, 25 to 58, 39 to 61, 48 to 73, 63 to 85, 72 to 94), mapOf(29 to 7, 37 to 15, 52 to 28, 67 to 33, 78 to 49, 86 to 54, 97 to 21)),
    SnakesBoardLayout(6, mapOf(4 to 14, 9 to 31, 20 to 38, 28 to 84, 40 to 59, 51 to 67, 63 to 81, 71 to 91), mapOf(17 to 7, 54 to 34, 62 to 19, 64 to 60, 87 to 24, 93 to 73, 95 to 75, 99 to 78)),
    SnakesBoardLayout(7, mapOf(3 to 96, 8 to 93, 14 to 88, 17 to 84, 20 to 81), mapOf(99 to 4, 95 to 6, 92 to 11, 89 to 15, 86 to 19))
)

val TenPlayerColors = listOf(
    Color(0xFF0088FF), // 0: Ludo King Blue
    Color(0xFFE6192E), // 1: Ludo King Red
    Color(0xFF00B84C), // 2: Ludo King Green
    Color(0xFFFFCC00), // 3: Ludo King Yellow
    Color(0xFFFF7A00), // 4: Ludo King Orange (5P/6P)
    Color(0xFF8A2BE2), // 5: Ludo King Purple (6P)
    Color(0xFF00E5FF), // 6: Cyan (7P)
    Color(0xFFFF2A85), // 7: Magenta (8P)
    Color(0xFF70E000), // 8: Lime (9P)
    Color(0xFF6C63FF)  // 9: Indigo (10P)
)

val TenPlayerColorNames = listOf(
    "Blue", "Red", "Green", "Yellow", "Orange",
    "Purple", "Cyan", "Pink", "Lime", "Indigo"
)

data class CareerStats(
    val playerName: String = "TrollDrako",
    val statusText: String = "Ludo Grandmaster",
    val country: String = "🇮🇳 India",
    val avatarIndex: Int = 0,
    val coins: Int = 2650,
    val gems: Int = 150,
    val xp: Int = 40,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val winStreak: Int = 0,
    val tokensCaptured: Int = 0,
    val tokensKilled: Int = 0,
    val tournamentsWon: Int = 0
) {
    val level: Int get() = (xp / 180) + 1
    val xpInCurrentLevel: Int get() = xp % 180
}

data class SavedGameRecord(
    val id: String,
    val timestamp: String,
    val playerCount: Int,
    val gameModeName: String,
    val oneTokenOut: Boolean,
    val friendlyKill: Boolean,
    val currentTurnIndex: Int,
    val playerNames: List<String>,
    val playerColorIndices: List<Int>,
    val playerBots: List<Boolean>,
    val tokenProgressFlat: List<Int>
)

data class PlayerLobbySlot(
    val slotIndex: Int,
    val name: String,
    val colorIndex: Int,
    val isBot: Boolean = false
)

data class Token(
    val id: Int,
    val playerIndex: Int,
    val progress: Int = -1
) {
    val isInBase: Boolean get() = progress == -1
    val isFinished: Boolean get() = progress == 56
    val isInHomeStretch: Boolean get() = progress in 51..55

    fun canMove(diceValue: Int): Boolean {
        if (isFinished) return false
        if (isInBase) return diceValue == 6
        return (progress + diceValue) <= 56
    }
}

data class Player(
    val index: Int,
    val name: String,
    val colorIndex: Int,
    val color: Color,
    val isBot: Boolean = false,
    val teamId: Int = index % 2,
    val diceValue: Int = 1,
    val isRolling: Boolean = false,
    val tokens: List<Token> = List(4) { Token(id = it, playerIndex = index, progress = -1) },
    val snakesPosition: Int = 1,
    val rank: Int? = null
) {
    fun hasMetWinCondition(mode: GameMode): Boolean {
        val finishedCount = tokens.count { it.isFinished }
        return finishedCount >= mode.tokensToWin
    }
}

val PresetAccentBubbles = listOf(
    Color(0xFF00F5D4), // Liquid Cyan
    Color(0xFFFFBE0B), // Royal Gold
    Color(0xFFFF2A85), // Neon Rose
    Color(0xFF3B82F6), // Electric Blue
    Color(0xFFA855F7)  // Ultra Violet
)

val AvatarEmojis = listOf(
    "🤠", "🦸‍♂️", "👸", "🧑‍🚀", "🕵️‍♂️", "🥷",
    "🧙‍♂️", "👩‍🎤", "🤴", "🧛‍♂️", "🧑‍✈️", "👑"
)

val CountryList = listOf(
    "🇮🇳 India", "🇺🇸 United States", "🇬🇧 United Kingdom", "🇨🇦 Canada",
    "🇦🇺 Australia", "🇧🇷 Brazil", "🇯🇵 Japan", "🇩🇪 Germany", "🇫🇷 France", "🇦🇪 UAE"
)

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.SPLASH,
    val bottomTab: BottomNavTab = BottomNavTab.HOME,
    val activeModal: ActiveModal = ActiveModal.NONE,
    val hubEntryType: HubEntryType = HubEntryType.PASS_N_PLAY,

    // Theme, Mode & Apple Liquid Glass Controls
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val glassValue: Float = 0.38f, // 0f = Clear, 0.5f = Frosted, 1f = Opaque
    val primaryThemeHue: Float = 218f,
    val customAccentColor: Color = Color(0xFFFFBE0B),
    val boardTheme: LudoBoardTheme = LudoBoardTheme.CLASSIC_ROYAL,

    // Audio, Vibration & Language Settings
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val selectedLanguage: String = "English",

    // Ludo Pre-Match Setup State
    val playerCount: Int = 4,
    val gameMode: GameMode = GameMode.CLASSIC,
    val tokenStyle: TokenStyle = TokenStyle.LOCATION_PIN,
    val lobbySlots: List<PlayerLobbySlot> = List(10) { idx ->
        PlayerLobbySlot(slotIndex = idx, name = "Player ${idx + 1}", colorIndex = idx, isBot = false)
    },
    val selectedTeamChoice: Int = 0,
    val friendlyKillEnabled: Boolean = false,
    val pendingOneTokenOut: Boolean = false,

    // Active Ludo Match State
    val players: List<Player> = emptyList(),
    val currentTurnIndex: Int = 0,
    val diceValue: Int = 1,
    val isRolling: Boolean = false,
    val hasRolledThisTurn: Boolean = false,
    val consecutiveSixes: Int = 0,
    val showGameStartBanner: Boolean = false,
    val showBottomLeftMenuPopup: Boolean = false,
    val statusMessage: String = "Tap Dice to Roll",
    val winnersOrder: List<Int> = emptyList(),
    val saveTimestampPreview: String = "",

    // Snakes & Ladders State
    val snakesThemeIndex: Int = 0,
    val snakesBoardIndex: Int = 0,

    // Career & Saved Games
    val careerStats: CareerStats = CareerStats(),
    val savedGames: List<SavedGameRecord> = emptyList()
) {
    val primaryThemeColor: Color
        get() = when (themeMode) {
            AppThemeMode.LIGHT -> Color.hsv(primaryThemeHue, 0.45f, 0.92f)
            else -> Color.hsv(primaryThemeHue, 0.78f, 0.28f)
        }

    val glassModeLabel: GlassMode
        get() = when {
            glassValue < 0.33f -> GlassMode.CLEAR_LIQUID
            glassValue < 0.72f -> GlassMode.FROSTED_TRANSLUCENT
            else -> GlassMode.OPAQUE_MATTE
        }
}
