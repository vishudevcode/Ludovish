package com.ludovish.app.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludovish.app.core.CareerManager
import com.ludovish.app.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class LudovishViewModel : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        _uiState.update { it.copy(isRolling = false) }
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var careerManager: CareerManager? = null
    private var botJob: Job? = null
    private var rushBotLoopJob: Job? = null

    private val safeStepOffsets = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    init {
        viewModelScope.launch {
            delay(2100)
            if (_uiState.value.currentScreen == AppScreen.SPLASH) {
                _uiState.update { it.copy(currentScreen = AppScreen.LUDO_HOME) }
            }
        }
    }

    fun attachCareerManager(manager: CareerManager) {
        if (careerManager == null) {
            careerManager = manager
            _uiState.update {
                it.copy(
                    careerStats = manager.loadStats(),
                    savedGames = manager.loadSavedGames()
                )
            }
        }
    }

    private fun updateCareer(transform: (CareerStats) -> CareerStats) {
        _uiState.update { state ->
            val updated = transform(state.careerStats)
            careerManager?.saveStats(updated)
            state.copy(careerStats = updated)
        }
    }

    fun skipSplash() {
        _uiState.update { it.copy(currentScreen = AppScreen.LUDO_HOME) }
    }

    fun switchMainHub(toSnakes: Boolean) {
        _uiState.update {
            it.copy(
                currentScreen = if (toSnakes) AppScreen.SNAKES_HOME else AppScreen.LUDO_HOME,
                activeModal = ActiveModal.NONE
            )
        }
    }

    fun selectBottomTab(tab: BottomNavTab) {
        val modal = when (tab) {
            BottomNavTab.HOME -> ActiveModal.NONE
            BottomNavTab.EVENT -> ActiveModal.THEME_COLOR_WHEEL
            BottomNavTab.ADDA -> ActiveModal.LOAD_SAVED_GAMES
            BottomNavTab.INVENTORY -> ActiveModal.LUDO_MODE_SELECT
            BottomNavTab.SOCIAL -> ActiveModal.STATISTICS
        }
        _uiState.update { it.copy(bottomTab = tab, activeModal = modal) }
    }

    fun openModal(modal: ActiveModal) {
        val timestamp = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH).format(Date())
        _uiState.update {
            it.copy(
                activeModal = modal,
                saveTimestampPreview = timestamp,
                showBottomLeftMenuPopup = false
            )
        }
    }

    fun closeModal() {
        _uiState.update { it.copy(activeModal = ActiveModal.NONE, bottomTab = BottomNavTab.HOME) }
    }

    fun toggleBottomLeftMenu() {
        _uiState.update { it.copy(showBottomLeftMenuPopup = !it.showBottomLeftMenuPopup) }
    }

    fun updateProfile(name: String, status: String, country: String, avatarIdx: Int) {
        updateCareer {
            it.copy(
                playerName = name.ifBlank { "TrollDrako" },
                statusText = status,
                country = country,
                avatarIndex = avatarIdx
            )
        }
    }

    fun spinDailyWheelReward(): Int {
        val reward = listOf(250, 500, 1200, 750, 1500).random()
        updateCareer { it.copy(coins = it.coins + reward, xp = it.xp + 30) }
        return reward
    }

    fun toggleSound(): Boolean {
        val next = !_uiState.value.soundEnabled
        _uiState.update { it.copy(soundEnabled = next) }
        return next
    }

    fun toggleMusic() {
        _uiState.update { it.copy(musicEnabled = !it.musicEnabled) }
    }

    fun toggleVibration() {
        _uiState.update { it.copy(vibrationEnabled = !it.vibrationEnabled) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun updateGlassSlider(value: Float) {
        _uiState.update { it.copy(glassValue = value.coerceIn(0f, 1f)) }
    }

    fun updatePrimaryThemeHue(hue: Float) {
        _uiState.update { it.copy(primaryThemeHue = hue.coerceIn(0f, 360f)) }
    }

    fun updateCustomAccentColor(color: Color) {
        _uiState.update { it.copy(customAccentColor = color) }
    }

    fun selectBoardTheme(theme: LudoBoardTheme) {
        _uiState.update { it.copy(boardTheme = theme) }
    }

    fun openLudoSetupForHub(entryType: HubEntryType) {
        val defaultMode = when (entryType) {
            HubEntryType.TEAM_UP -> GameMode.TEAM_UP
            else -> GameMode.CLASSIC
        }
        val slots = List(10) { idx ->
            val isBot = when (entryType) {
                HubEntryType.COMPUTER, HubEntryType.TOURNAMENT -> idx > 0
                else -> false
            }
            val name = when {
                entryType == HubEntryType.COMPUTER && idx == 0 -> "You"
                entryType == HubEntryType.COMPUTER -> "Computer ${if (idx == 1) "" else idx}".trim()
                else -> "Player ${idx + 1}"
            }
            PlayerLobbySlot(slotIndex = idx, name = name, colorIndex = idx, isBot = isBot)
        }
        _uiState.update {
            it.copy(
                hubEntryType = entryType,
                gameMode = defaultMode,
                playerCount = if (entryType == HubEntryType.COMPUTER) 2 else 4,
                lobbySlots = slots,
                activeModal = ActiveModal.LUDO_MODE_SELECT
            )
        }
    }

    fun selectTokenStyle(style: TokenStyle) {
        _uiState.update { it.copy(tokenStyle = style) }
    }

    fun selectGameMode(mode: GameMode) {
        _uiState.update {
            it.copy(
                gameMode = mode,
                playerCount = if (mode == GameMode.TEAM_UP) 4 else it.playerCount
            )
        }
    }

    fun proceedToPlayerSetup() {
        _uiState.update { it.copy(activeModal = ActiveModal.LUDO_PLAYER_SETUP) }
    }

    fun setLobbyPlayerCount(count: Int) {
        _uiState.update { it.copy(playerCount = count.coerceIn(2, 10)) }
    }

    fun updateLobbySlotName(slotIndex: Int, newName: String) {
        _uiState.update { state ->
            val updated = state.lobbySlots.map {
                if (it.slotIndex == slotIndex) it.copy(name = newName) else it
            }
            state.copy(lobbySlots = updated)
        }
    }

    fun cycleLobbySlotColor(slotIndex: Int) {
        _uiState.update { state ->
            val updated = state.lobbySlots.map {
                if (it.slotIndex == slotIndex) {
                    it.copy(colorIndex = (it.colorIndex + 1) % TenPlayerColors.size)
                } else it
            }
            state.copy(lobbySlots = updated)
        }
    }

    fun toggleLobbySlotBot(slotIndex: Int) {
        _uiState.update { state ->
            val updated = state.lobbySlots.map {
                if (it.slotIndex == slotIndex) {
                    val nextBot = !it.isBot
                    val nextName = if (nextBot) "Bot" else "Player ${slotIndex + 1}"
                    it.copy(isBot = nextBot, name = nextName)
                } else it
            }
            state.copy(lobbySlots = updated)
        }
    }

    fun toggleFriendlyKill() {
        _uiState.update { it.copy(friendlyKillEnabled = !it.friendlyKillEnabled) }
    }

    fun requestStartLudoMatch(oneTokenOut: Boolean) {
        if (oneTokenOut) {
            _uiState.update {
                it.copy(
                    pendingOneTokenOut = true,
                    activeModal = ActiveModal.KEEP_ONE_TOKEN_OUT_PROMPT
                )
            }
        } else {
            launchLudoMatch(oneTokenOut = false)
        }
    }

    fun confirmKeepOneTokenOutAndStart() {
        launchLudoMatch(oneTokenOut = true)
    }

    fun launchLudoMatch(oneTokenOut: Boolean) {
        botJob?.cancel()
        rushBotLoopJob?.cancel()

        val state = _uiState.value
        val count = state.playerCount.coerceIn(2, 10)
        val players = List(count) { idx ->
            val slot = state.lobbySlots[idx]
            val pColor = TenPlayerColors[slot.colorIndex % TenPlayerColors.size]
            val tokens = List(4) { tId ->
                val startOut = (oneTokenOut || state.gameMode == GameMode.QUICK || state.gameMode == GameMode.RUSH_MODE) && tId == 0
                Token(id = tId, playerIndex = idx, progress = if (startOut) 0 else -1)
            }
            Player(
                index = idx,
                name = slot.name.ifBlank { "Player ${idx + 1}" },
                colorIndex = slot.colorIndex,
                color = pColor,
                isBot = slot.isBot,
                teamId = idx % 2,
                diceValue = 1,
                tokens = tokens
            )
        }

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.LUDO_GAMEPLAY,
                activeModal = ActiveModal.NONE,
                players = players,
                currentTurnIndex = 0,
                diceValue = 1,
                isRolling = false,
                hasRolledThisTurn = false,
                consecutiveSixes = 0,
                winnersOrder = emptyList(),
                showGameStartBanner = true,
                showBottomLeftMenuPopup = false,
                statusMessage = "${players[0].name}'s Turn"
            )
        }

        viewModelScope.launch {
            delay(1450)
            _uiState.update { it.copy(showGameStartBanner = false) }
            if (_uiState.value.gameMode == GameMode.RUSH_MODE) {
                startRushModeAutonomousBotLoop()
            } else {
                triggerBotIfNeeded()
            }
        }
    }

    fun saveCurrentGameAndExit() {
        val state = _uiState.value
        if (state.players.isEmpty()) {
            exitToHome()
            return
        }
        val record = SavedGameRecord(
            id = "save_${System.currentTimeMillis()}",
            timestamp = state.saveTimestampPreview.ifBlank {
                SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH).format(Date())
            },
            playerCount = state.playerCount,
            gameModeName = state.gameMode.name,
            oneTokenOut = state.pendingOneTokenOut,
            friendlyKill = state.friendlyKillEnabled,
            currentTurnIndex = state.currentTurnIndex,
            playerNames = state.players.map { it.name },
            playerColorIndices = state.players.map { it.colorIndex },
            playerBots = state.players.map { it.isBot },
            tokenProgressFlat = state.players.flatMap { p -> p.tokens.map { it.progress } }
        )
        val updatedList = listOf(record) + state.savedGames
        careerManager?.saveGamesList(updatedList)
        _uiState.update {
            it.copy(
                savedGames = updatedList,
                currentScreen = AppScreen.LUDO_HOME,
                activeModal = ActiveModal.NONE,
                showBottomLeftMenuPopup = false
            )
        }
    }

    fun deleteSavedGame(id: String) {
        val updated = _uiState.value.savedGames.filterNot { it.id == id }
        careerManager?.saveGamesList(updated)
        _uiState.update { it.copy(savedGames = updated) }
    }

    fun resumeSavedGame(record: SavedGameRecord) {
        botJob?.cancel()
        rushBotLoopJob?.cancel()

        val mode = runCatching { GameMode.valueOf(record.gameModeName) }.getOrDefault(GameMode.CLASSIC)
        val count = record.playerCount.coerceIn(2, 10)
        val players = List(count) { pIdx ->
            val cIdx = record.playerColorIndices.getOrElse(pIdx) { pIdx }
            val tokens = List(4) { tIdx ->
                val flatIdx = pIdx * 4 + tIdx
                val prog = record.tokenProgressFlat.getOrElse(flatIdx) { -1 }
                Token(id = tIdx, playerIndex = pIdx, progress = prog)
            }
            Player(
                index = pIdx,
                name = record.playerNames.getOrElse(pIdx) { "Player ${pIdx + 1}" },
                colorIndex = cIdx,
                color = TenPlayerColors[cIdx % TenPlayerColors.size],
                isBot = record.playerBots.getOrElse(pIdx) { false },
                teamId = pIdx % 2,
                tokens = tokens
            )
        }

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.LUDO_GAMEPLAY,
                activeModal = ActiveModal.NONE,
                playerCount = count,
                gameMode = mode,
                friendlyKillEnabled = record.friendlyKill,
                players = players,
                currentTurnIndex = record.currentTurnIndex.coerceIn(0, count - 1),
                hasRolledThisTurn = false,
                isRolling = false,
                showGameStartBanner = true
            )
        }
        viewModelScope.launch {
            delay(1100)
            _uiState.update { it.copy(showGameStartBanner = false) }
            triggerBotIfNeeded()
        }
    }

    fun exitToHome() {
        botJob?.cancel()
        rushBotLoopJob?.cancel()
        _uiState.update {
            it.copy(
                currentScreen = if (it.currentScreen == AppScreen.SNAKES_GAMEPLAY) AppScreen.SNAKES_HOME else AppScreen.LUDO_HOME,
                activeModal = ActiveModal.NONE,
                showBottomLeftMenuPopup = false
            )
        }
    }

    fun rollDiceForPlayer(
        playerIndex: Int = _uiState.value.currentTurnIndex,
        onDiceEffect: () -> Unit = {},
        onStepEffect: (Int) -> Unit = {},
        onCaptureEffect: () -> Unit = {},
        onVictoryEffect: () -> Unit = {}
    ) {
        val state = _uiState.value
        if (state.gameMode == GameMode.RUSH_MODE) {
            rollRushModeForPlayer(playerIndex, onDiceEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
            return
        }

        if (playerIndex != state.currentTurnIndex || state.isRolling || state.hasRolledThisTurn) return

        viewModelScope.launch(exceptionHandler) {
            onDiceEffect()
            _uiState.update { it.copy(isRolling = true) }

            withContext(Dispatchers.Default) {
                repeat(7) {
                    delay(40)
                    val tempFace = Random.nextInt(1, 7)
                    _uiState.update { s ->
                        s.copy(
                            diceValue = tempFace,
                            players = s.players.map { p ->
                                if (p.index == playerIndex) p.copy(diceValue = tempFace, isRolling = true) else p
                            }
                        )
                    }
                }
            }

            val rolled = Random.nextInt(1, 7)
            val currentPlayer = _uiState.value.players[playerIndex]
            val newSixCount = if (rolled == 6) state.consecutiveSixes + 1 else 0

            if (newSixCount >= 3) {
                _uiState.update { s ->
                    s.copy(
                        diceValue = rolled,
                        isRolling = false,
                        hasRolledThisTurn = false,
                        consecutiveSixes = 0,
                        players = s.players.map { p ->
                            if (p.index == playerIndex) p.copy(diceValue = rolled, isRolling = false) else p
                        }
                    )
                }
                delay(550)
                advanceToNextTurn(onDiceEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
                return@launch
            }

            val movable = currentPlayer.tokens.filter { it.canMove(rolled) }
            _uiState.update { s ->
                s.copy(
                    diceValue = rolled,
                    isRolling = false,
                    hasRolledThisTurn = true,
                    consecutiveSixes = newSixCount,
                    players = s.players.map { p ->
                        if (p.index == playerIndex) p.copy(diceValue = rolled, isRolling = false) else p
                    }
                )
            }

            if (movable.isEmpty()) {
                delay(600)
                advanceToNextTurn(onDiceEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
            } else if (movable.size == 1 || currentPlayer.isBot) {
                delay(if (currentPlayer.isBot) 380 else 190)
                val chosen = chooseBestToken(currentPlayer, movable, rolled)
                moveToken(chosen.playerIndex, chosen.id, onDiceEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
            }
        }
    }

    private fun rollRushModeForPlayer(
        playerIndex: Int,
        onDiceEffect: () -> Unit,
        onStepEffect: (Int) -> Unit,
        onCaptureEffect: () -> Unit,
        onVictoryEffect: () -> Unit
    ) {
        val player = _uiState.value.players.getOrNull(playerIndex) ?: return
        if (player.isRolling) return

        viewModelScope.launch(exceptionHandler) {
            onDiceEffect()
            _uiState.update { s ->
                s.copy(players = s.players.map { if (it.index == playerIndex) it.copy(isRolling = true) else it })
            }
            delay(240)
            val rolled = Random.nextInt(1, 7)
            _uiState.update { s ->
                s.copy(players = s.players.map { if (it.index == playerIndex) it.copy(diceValue = rolled, isRolling = false) else it })
            }
            val updatedPlayer = _uiState.value.players[playerIndex]
            val movable = updatedPlayer.tokens.filter { it.canMove(rolled) }
            if (movable.isNotEmpty()) {
                val chosen = chooseBestToken(updatedPlayer, movable, rolled)
                stepAndResolveToken(playerIndex, chosen.id, rolled, onStepEffect, onCaptureEffect, onVictoryEffect)
            }
        }
    }

    private fun startRushModeAutonomousBotLoop() {
        rushBotLoopJob?.cancel()
        rushBotLoopJob = viewModelScope.launch(exceptionHandler) {
            while (isActive && _uiState.value.currentScreen == AppScreen.LUDO_GAMEPLAY && _uiState.value.gameMode == GameMode.RUSH_MODE) {
                delay(1350)
                val bots = _uiState.value.players.filter { it.isBot && !it.hasMetWinCondition(GameMode.RUSH_MODE) }
                bots.forEach { bot ->
                    launch {
                        delay(Random.nextLong(100, 600))
                        rollRushModeForPlayer(bot.index, {}, {}, {}, {})
                    }
                }
            }
        }
    }

    private fun chooseBestToken(player: Player, candidates: List<Token>, dice: Int): Token {
        candidates.firstOrNull { it.progress + dice == 56 }?.let { return it }
        if (dice == 6) {
            candidates.firstOrNull { it.isInBase }?.let { return it }
        }
        return candidates.maxByOrNull { it.progress } ?: candidates.first()
    }

    fun moveToken(
        playerIndex: Int,
        tokenId: Int,
        onDiceEffect: () -> Unit = {},
        onStepEffect: (Int) -> Unit = {},
        onCaptureEffect: () -> Unit = {},
        onVictoryEffect: () -> Unit = {}
    ) {
        val state = _uiState.value
        if (state.gameMode != GameMode.RUSH_MODE) {
            if (playerIndex != state.currentTurnIndex || !state.hasRolledThisTurn || state.isRolling) return
        }
        val player = state.players.getOrNull(playerIndex) ?: return
        val token = player.tokens.firstOrNull { it.id == tokenId } ?: return
        val dice = if (state.gameMode == GameMode.RUSH_MODE) player.diceValue else state.diceValue
        if (!token.canMove(dice)) return

        viewModelScope.launch(exceptionHandler) {
            _uiState.update { it.copy(isRolling = true) }
            val earnedBonus = stepAndResolveToken(playerIndex, tokenId, dice, onStepEffect, onCaptureEffect, onVictoryEffect)
            _uiState.update { it.copy(isRolling = false) }

            if (state.gameMode != GameMode.RUSH_MODE) {
                if (earnedBonus) {
                    _uiState.update { it.copy(hasRolledThisTurn = false) }
                    triggerBotIfNeeded(onDiceEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
                } else {
                    advanceToNextTurn(onDiceEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
                }
            }
        }
    }

    private suspend fun stepAndResolveToken(
        playerIndex: Int,
        tokenId: Int,
        dice: Int,
        onStepEffect: (Int) -> Unit,
        onCaptureEffect: () -> Unit,
        onVictoryEffect: () -> Unit
    ): Boolean {
        val player = _uiState.value.players.getOrNull(playerIndex) ?: return false
        val token = player.tokens.firstOrNull { it.id == tokenId } ?: return false

        if (token.isInBase && dice == 6) {
            onStepEffect(0)
            updateTokenProgress(playerIndex, tokenId, 0)
            return true
        }

        val start = token.progress
        val target = start + dice
        var hop = 0
        for (step in (start + 1)..target) {
            onStepEffect(hop++)
            updateTokenProgress(playerIndex, tokenId, step)
            delay(68)
        }

        var captured = false
        if (target in 0..50) {
            captured = checkAndExecuteCapture(playerIndex, target)
            if (captured) {
                onCaptureEffect()
                if (!player.isBot) {
                    updateCareer { it.copy(tokensCaptured = it.tokensCaptured + 1, coins = it.coins + 50, xp = it.xp + 20) }
                }
            }
        }

        val reachedHome = target == 56
        if (reachedHome) onVictoryEffect()

        val updatedPlayer = _uiState.value.players[playerIndex]
        val mode = _uiState.value.gameMode
        if (updatedPlayer.hasMetWinCondition(mode) && playerIndex !in _uiState.value.winnersOrder) {
            onVictoryEffect()
            val newWinners = _uiState.value.winnersOrder + playerIndex
            val rank = newWinners.size
            _uiState.update { s ->
                s.copy(
                    winnersOrder = newWinners,
                    players = s.players.map { if (it.index == playerIndex) it.copy(rank = rank) else it }
                )
            }
            if (!updatedPlayer.isBot && rank == 1) {
                updateCareer {
                    it.copy(
                        gamesWon = it.gamesWon + 1,
                        winStreak = it.winStreak + 1,
                        coins = it.coins + 400,
                        xp = it.xp + 120
                    )
                }
            }
        }

        return (dice == 6 || captured || reachedHome) && !_uiState.value.players[playerIndex].hasMetWinCondition(mode)
    }

    private fun updateTokenProgress(playerIndex: Int, tokenId: Int, newProgress: Int) {
        _uiState.update { state ->
            state.copy(
                players = state.players.map { p ->
                    if (p.index != playerIndex) p
                    else p.copy(tokens = p.tokens.map { t -> if (t.id == tokenId) t.copy(progress = newProgress) else t })
                }
            )
        }
    }

    private fun toGlobalTrackPosition(playerIndex: Int, relativeProgress: Int, totalPlayers: Int): Int {
        val ringSize = 52
        val offset = (playerIndex * (ringSize / totalPlayers.coerceAtLeast(2))) % ringSize
        return (offset + relativeProgress) % ringSize
    }

    private fun checkAndExecuteCapture(activePlayerIndex: Int, targetRelativeProgress: Int): Boolean {
        if (targetRelativeProgress in safeStepOffsets) return false
        val state = _uiState.value
        val activePlayer = state.players[activePlayerIndex]
        val activeGlobal = toGlobalTrackPosition(activePlayerIndex, targetRelativeProgress, state.playerCount)

        var anyCaptured = false
        val updatedPlayers = state.players.map { opponent ->
            val isTeammate = state.gameMode == GameMode.TEAM_UP && opponent.teamId == activePlayer.teamId
            if (opponent.index == activePlayerIndex || (isTeammate && !state.friendlyKillEnabled)) {
                opponent
            } else {
                val victims = opponent.tokens.filter { t ->
                    t.progress in 0..50 &&
                        t.progress !in safeStepOffsets &&
                        toGlobalTrackPosition(opponent.index, t.progress, state.playerCount) == activeGlobal
                }
                if (victims.size == 1) {
                    anyCaptured = true
                    val vId = victims.first().id
                    opponent.copy(tokens = opponent.tokens.map { if (it.id == vId) it.copy(progress = -1) else it })
                } else opponent
            }
        }
        if (anyCaptured) {
            _uiState.update { it.copy(players = updatedPlayers) }
        }
        return anyCaptured
    }

    private fun advanceToNextTurn(
        onDiceEffect: () -> Unit,
        onStepEffect: (Int) -> Unit,
        onCaptureEffect: () -> Unit,
        onVictoryEffect: () -> Unit
    ) {
        val state = _uiState.value
        val activeRemaining = state.players.filter { !it.hasMetWinCondition(state.gameMode) }
        if (activeRemaining.size <= 1) return

        var nextIdx = (state.currentTurnIndex + 1) % state.playerCount
        var guard = 0
        while (state.players[nextIdx].hasMetWinCondition(state.gameMode) && guard < state.playerCount) {
            nextIdx = (nextIdx + 1) % state.playerCount
            guard++
        }

        _uiState.update {
            it.copy(
                currentTurnIndex = nextIdx,
                hasRolledThisTurn = false,
                isRolling = false,
                consecutiveSixes = 0
            )
        }
        triggerBotIfNeeded(onDiceEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
    }

    private fun triggerBotIfNeeded(
        onDiceEffect: () -> Unit = {},
        onStepEffect: (Int) -> Unit = {},
        onCaptureEffect: () -> Unit = {},
        onVictoryEffect: () -> Unit = {}
    ) {
        botJob?.cancel()
        val state = _uiState.value
        if (state.currentScreen != AppScreen.LUDO_GAMEPLAY || state.gameMode == GameMode.RUSH_MODE) return
        val current = state.players.getOrNull(state.currentTurnIndex) ?: return
        if (!current.isBot || state.hasRolledThisTurn || state.isRolling) return

        botJob = viewModelScope.launch(exceptionHandler) {
            delay(520)
            rollDiceForPlayer(current.index, onDiceEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
        }
    }

    fun openSnakesThemeSelector() {
        _uiState.update { it.copy(currentScreen = AppScreen.SNAKES_THEME_SELECT) }
    }

    fun cycleSnakesTheme(delta: Int) {
        val count = SnakesTheme.entries.size
        _uiState.update {
            it.copy(snakesThemeIndex = (it.snakesThemeIndex + delta + count) % count)
        }
    }

    fun openSnakesBoardSelector() {
        _uiState.update { it.copy(currentScreen = AppScreen.SNAKES_BOARD_SELECT) }
    }

    fun cycleSnakesBoard(delta: Int) {
        val count = SevenSnakesBoards.size
        _uiState.update {
            it.copy(snakesBoardIndex = (it.snakesBoardIndex + delta + count) % count)
        }
    }

    fun startSnakesMatch(vsComputer: Boolean) {
        val players = listOf(
            Player(index = 0, name = "You", colorIndex = 0, color = TenPlayerColors[0], isBot = false, snakesPosition = 1),
            Player(index = 1, name = if (vsComputer) "Computer" else "Player 2", colorIndex = 1, color = TenPlayerColors[1], isBot = vsComputer, snakesPosition = 1)
        )
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.SNAKES_GAMEPLAY,
                players = players,
                playerCount = 2,
                currentTurnIndex = 0,
                diceValue = 1,
                isRolling = false,
                hasRolledThisTurn = false
            )
        }
    }

    fun rollSnakesDice(
        onDiceEffect: () -> Unit,
        onStepEffect: (Int) -> Unit,
        onSlideOrClimbEffect: () -> Unit,
        onVictoryEffect: () -> Unit
    ) {
        val state = _uiState.value
        if (state.isRolling) return
        val playerIndex = state.currentTurnIndex
        val player = state.players.getOrNull(playerIndex) ?: return
        val layout = SevenSnakesBoards[state.snakesBoardIndex % SevenSnakesBoards.size]

        viewModelScope.launch(exceptionHandler) {
            onDiceEffect()
            _uiState.update { it.copy(isRolling = true) }
            delay(280)
            val rolled = Random.nextInt(1, 7)
            _uiState.update { it.copy(diceValue = rolled) }

            val startPos = player.snakesPosition
            val targetPos = startPos + rolled
            if (targetPos <= 100) {
                for (sq in (startPos + 1)..targetPos) {
                    onStepEffect(sq - startPos)
                    updateSnakesPlayerPos(playerIndex, sq)
                    delay(85)
                }

                val finalSquare = layout.ladders[targetPos] ?: layout.snakes[targetPos] ?: targetPos
                if (finalSquare != targetPos) {
                    delay(220)
                    onSlideOrClimbEffect()
                    updateSnakesPlayerPos(playerIndex, finalSquare)
                    delay(300)
                }

                if (finalSquare == 100) {
                    onVictoryEffect()
                    updateCareer { it.copy(gamesWon = it.gamesWon + 1, coins = it.coins + 300) }
                    _uiState.update { it.copy(isRolling = false, statusMessage = "🏆 ${player.name} Reached 100!") }
                    return@launch
                }
            }

            val nextTurn = if (rolled == 6) playerIndex else (playerIndex + 1) % state.players.size
            _uiState.update { it.copy(currentTurnIndex = nextTurn, isRolling = false) }

            val nextPlayer = _uiState.value.players[nextTurn]
            if (nextPlayer.isBot) {
                delay(550)
                rollSnakesDice(onDiceEffect, onStepEffect, onSlideOrClimbEffect, onVictoryEffect)
            }
        }
    }

    private fun updateSnakesPlayerPos(playerIndex: Int, square: Int) {
        _uiState.update { s ->
            s.copy(
                players = s.players.map { if (it.index == playerIndex) it.copy(snakesPosition = square) else it }
            )
        }
    }
}
