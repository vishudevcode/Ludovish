package com.ludovish.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludovish.app.core.CareerManager
import com.ludovish.app.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class LudovishViewModel : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        _uiState.update { it.copy(isRolling = false, statusMessage = "Ready") }
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var careerManager: CareerManager? = null
    private var lastSnapshot: TurnSnapshot? = null
    private var botJob: Job? = null

    private val safeStepOffsets = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    init {
        startNewMatch(playerCount = 4, mode = GameMode.CLASSIC)
    }

    fun attachCareerManager(manager: CareerManager) {
        if (careerManager == null) {
            careerManager = manager
            _uiState.update { it.copy(careerStats = manager.loadStats()) }
        }
    }

    private fun updateCareer(transform: (CareerStats) -> CareerStats) {
        _uiState.update { state ->
            val updated = transform(state.careerStats)
            careerManager?.saveStats(updated)
            state.copy(careerStats = updated)
        }
    }

    fun startNewMatch(
        playerCount: Int = _uiState.value.playerCount,
        mode: GameMode = _uiState.value.gameMode
    ) {
        botJob?.cancel()
        lastSnapshot = null
        val clampedCount = playerCount.coerceIn(2, 10)
        val existingPlayers = _uiState.value.players

        val players = List(clampedCount) { index ->
            val wasBot = existingPlayers.getOrNull(index)?.isBot ?: (index > 0)
            val customName = existingPlayers.getOrNull(index)?.name ?: TenPlayerNames[index]
            val initialTokens = List(4) { tokenId ->
                // In Quick Ludo Mode, Token #0 starts pre-deployed on the start square (progress = 0)
                val initialProgress = if (mode == GameMode.QUICK_RUSH && tokenId == 0) 0 else -1
                Token(id = tokenId, playerIndex = index, progress = initialProgress)
            }
            Player(
                index = index,
                name = customName,
                color = TenPlayerColors[index],
                isBot = wasBot,
                hasCapturedEnemy = false,
                tokens = initialTokens,
                rank = null
            )
        }

        _uiState.update {
            it.copy(
                playerCount = clampedCount,
                gameMode = mode,
                players = players,
                currentTurnIndex = 0,
                diceValue = 6,
                isRolling = false,
                hasRolledThisTurn = false,
                consecutiveSixes = 0,
                winnersOrder = emptyList(),
                canUndo = false,
                statusMessage = "${players[0].name} • ${mode.title} Mode"
            )
        }
    }

    fun setPlayerCount(count: Int) {
        startNewMatch(playerCount = count, mode = _uiState.value.gameMode)
    }

    fun selectGameMode(mode: GameMode) {
        startNewMatch(playerCount = _uiState.value.playerCount, mode = mode)
    }

    fun selectTokenStyle(style: TokenStyle) {
        _uiState.update { it.copy(tokenStyle = style) }
    }

    fun selectDiceStyle(style: DiceStyle) {
        _uiState.update { it.copy(diceStyle = style) }
    }

    fun selectBotDifficulty(difficulty: BotDifficulty) {
        _uiState.update { it.copy(botDifficulty = difficulty) }
    }

    /** Toggle any individual seat between Human and AI Bot */
    fun togglePlayerBotStatus(
        playerIndex: Int,
        onDiceEffect: () -> Unit = {},
        onBonusSixEffect: () -> Unit = {},
        onStepEffect: (Int) -> Unit = {},
        onCaptureEffect: () -> Unit = {},
        onVictoryEffect: () -> Unit = {}
    ) {
        _uiState.update { state ->
            val updated = state.players.map { p ->
                if (p.index == playerIndex) p.copy(isBot = !p.isBot) else p
            }
            state.copy(players = updated)
        }
        triggerBotIfNeeded(onDiceEffect, onBonusSixEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
    }

    /** Rewind/Undo last move for 100 Coins */
    fun undoLastMove(): Boolean {
        val snap = lastSnapshot ?: return false
        val stats = _uiState.value.careerStats
        if (stats.coins < 100 || _uiState.value.isRolling) return false

        botJob?.cancel()
        updateCareer { it.copy(coins = (it.coins - 100).coerceAtLeast(0)) }
        _uiState.update {
            it.copy(
                players = snap.players,
                currentTurnIndex = snap.currentTurnIndex,
                diceValue = snap.diceValue,
                winnersOrder = snap.winnersOrder,
                hasRolledThisTurn = false,
                isRolling = false,
                canUndo = false,
                statusMessage = "Move Rewound (-100 Coins) • Roll Again!"
            )
        }
        lastSnapshot = null
        return true
    }

    fun toggleSoundEnabled(): Boolean {
        val next = !_uiState.value.soundEnabled
        _uiState.update { it.copy(soundEnabled = next) }
        return next
    }

    fun updateSoundVolume(volume: Float) {
        _uiState.update { it.copy(soundVolume = volume.coerceIn(0f, 1f)) }
    }

    fun updateGlassSlider(value: Float) {
        _uiState.update { it.copy(glassValue = value.coerceIn(0f, 1f)) }
    }

    fun selectPalette(palette: ThemePalette) {
        _uiState.update { it.copy(selectedPalette = palette, useCustomColor = false) }
    }

    fun updateCustomHue(hue: Float) {
        _uiState.update { it.copy(customHue = hue.coerceIn(0f, 360f), useCustomColor = true) }
    }

    fun selectDockSection(section: DockSection) {
        _uiState.update { it.copy(activeSheet = section) }
    }

    fun rollDice(
        onDiceEffect: () -> Unit,
        onBonusSixEffect: () -> Unit,
        onStepEffect: (Int) -> Unit,
        onCaptureEffect: () -> Unit,
        onVictoryEffect: () -> Unit
    ) {
        val state = _uiState.value
        if (state.isRolling || state.hasRolledThisTurn) return

        // Save snapshot before human turn for Undo support
        val currentPlayer = state.players.getOrNull(state.currentTurnIndex) ?: return
        if (!currentPlayer.isBot) {
            lastSnapshot = TurnSnapshot(
                players = state.players,
                currentTurnIndex = state.currentTurnIndex,
                diceValue = state.diceValue,
                winnersOrder = state.winnersOrder
            )
        }

        viewModelScope.launch(exceptionHandler) {
            onDiceEffect()
            _uiState.update { it.copy(isRolling = true, statusMessage = "Rolling...") }

            withContext(Dispatchers.Default) {
                repeat(7) {
                    delay(40)
                    _uiState.update { s -> s.copy(diceValue = Random.nextInt(1, 7)) }
                }
            }

            val rolled = Random.nextInt(1, 7)
            if (rolled == 6) {
                onBonusSixEffect()
                if (!currentPlayer.isBot) {
                    updateCareer { it.copy(sixesRolled = it.sixesRolled + 1, xp = it.xp + 5) }
                }
            }

            val newSixCount = if (rolled == 6) state.consecutiveSixes + 1 else 0
            if (newSixCount >= 3) {
                _uiState.update {
                    it.copy(
                        diceValue = rolled,
                        isRolling = false,
                        hasRolledThisTurn = false,
                        consecutiveSixes = 0,
                        statusMessage = "Three 6s! Turn forfeited"
                    )
                }
                delay(650)
                advanceToNextTurn(onDiceEffect, onBonusSixEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
                return@launch
            }

            // Team Up Mode: If current player finished, control teammate's tokens!
            val effectiveController = resolveControllingPlayer(currentPlayer, _uiState.value)
            val movableTokens = effectiveController.tokens.filter {
                it.canMove(rolled, state.gameMode, effectiveController.hasCapturedEnemy)
            }

            _uiState.update {
                it.copy(
                    diceValue = rolled,
                    isRolling = false,
                    hasRolledThisTurn = true,
                    consecutiveSixes = newSixCount,
                    canUndo = lastSnapshot != null,
                    statusMessage = if (movableTokens.isEmpty()) {
                        if (state.gameMode == GameMode.MASTER && !effectiveController.hasCapturedEnemy)
                            "Rolled $rolled — Capture an enemy to enter Home!"
                        else "Rolled $rolled — No movable tokens"
                    } else {
                        "Rolled $rolled — Tap a glowing ${effectiveController.name} token"
                    }
                )
            }

            if (movableTokens.isEmpty()) {
                delay(700)
                advanceToNextTurn(onDiceEffect, onBonusSixEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
            } else if (movableTokens.size == 1 || currentPlayer.isBot) {
                delay(if (currentPlayer.isBot) 420 else 220)
                val chosen = chooseSmartAiToken(effectiveController, movableTokens, rolled, _uiState.value)
                moveToken(chosen.id, onDiceEffect, onBonusSixEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
            }
        }
    }

    private fun resolveControllingPlayer(player: Player, state: GameUiState): Player {
        if (state.gameMode == GameMode.TEAM_UP && player.hasMetWinCondition(state.gameMode)) {
            val teammateIdx = (player.index + 2) % state.playerCount
            val teammate = state.players.getOrNull(teammateIdx)
            if (teammate != null && !teammate.hasMetWinCondition(state.gameMode)) {
                return teammate
            }
        }
        return player
    }

    /** Multi-tier Offline AI Heuristics (Easy, Tactical, Grandmaster) */
    private fun chooseSmartAiToken(
        player: Player,
        candidates: List<Token>,
        dice: Int,
        state: GameUiState
    ): Token {
        if (state.botDifficulty == BotDifficulty.EASY) {
            return candidates.random()
        }

        return candidates.maxByOrNull { token ->
            var score = 0
            val target = if (token.isInBase) 0 else token.progress + dice

            // 1. Highest priority: Entering Crown (56)
            if (target == 56) score += 500

            // 2. High priority: Capturing an opponent token
            if (target in 0..50 && target !in safeStepOffsets) {
                val globalTarget = toGlobalTrackPosition(player.index, target, state.playerCount)
                val canCapture = state.players.any { opp ->
                    opp.index != player.index &&
                        !areAllies(player.index, opp.index, state.gameMode) &&
                        opp.tokens.count {
                            it.progress in 0..50 &&
                                it.progress !in safeStepOffsets &&
                                toGlobalTrackPosition(opp.index, it.progress, state.playerCount) == globalTarget
                        } == 1
                }
                if (canCapture) score += 420
            }

            // 3. Deploying from Home Base on a 6
            if (token.isInBase && dice == 6) score += 280

            // 4. Landing on a Star Safe Zone
            if (target in safeStepOffsets) score += 190

            // 5. Grandmaster AI: Escape danger if currently on an unsafe tile near an enemy
            if (state.botDifficulty == BotDifficulty.GRANDMASTER && token.progress in 1..50 && token.progress !in safeStepOffsets) {
                score += 95
            }

            // 6. Prefer advancing the lead token
            score += token.progress * 2
            score
        } ?: candidates.first()
    }

    fun moveToken(
        tokenId: Int,
        onDiceEffect: () -> Unit,
        onBonusSixEffect: () -> Unit,
        onStepEffect: (Int) -> Unit,
        onCaptureEffect: () -> Unit,
        onVictoryEffect: () -> Unit
    ) {
        val state = _uiState.value
        if (!state.hasRolledThisTurn || state.isRolling) return

        val turnOwner = state.players.getOrNull(state.currentTurnIndex) ?: return
        val controller = resolveControllingPlayer(turnOwner, state)
        val playerIndex = controller.index
        val token = controller.tokens.firstOrNull { it.id == tokenId } ?: return
        val dice = state.diceValue

        if (!token.canMove(dice, state.gameMode, controller.hasCapturedEnemy)) return

        viewModelScope.launch(exceptionHandler) {
            _uiState.update { it.copy(isRolling = true) }

            if (token.isInBase && dice == 6) {
                onStepEffect(0)
                onBonusSixEffect()
                updateTokenProgress(playerIndex, tokenId, 0)
                _uiState.update {
                    it.copy(
                        isRolling = false,
                        hasRolledThisTurn = false,
                        statusMessage = "${controller.name} deployed a token! Bonus Roll!"
                    )
                }
                triggerBotIfNeeded(onDiceEffect, onBonusSixEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
                return@launch
            }

            val startProgress = token.progress
            val targetProgress = startProgress + dice
            var hopIndex = 0
            for (step in (startProgress + 1)..targetProgress) {
                onStepEffect(hopIndex++)
                updateTokenProgress(playerIndex, tokenId, step)
                delay(72)
            }

            var capturedOpponent = false
            if (targetProgress in 0..50) {
                capturedOpponent = checkAndExecuteCapture(playerIndex, targetProgress)
                if (capturedOpponent) {
                    onCaptureEffect()
                    if (!turnOwner.isBot) {
                        updateCareer {
                            it.copy(
                                tokensCaptured = it.tokensCaptured + 1,
                                coins = it.coins + 50,
                                xp = it.xp + 25
                            )
                        }
                    }
                }
            }

            val reachedCrown = targetProgress == 56
            if (reachedCrown) {
                onVictoryEffect()
                if (!turnOwner.isBot) {
                    updateCareer { it.copy(coins = it.coins + 100, xp = it.xp + 40) }
                }
            }

            val updatedController = _uiState.value.players[playerIndex]
            val currentMode = _uiState.value.gameMode
            if (updatedController.hasMetWinCondition(currentMode) && playerIndex !in _uiState.value.winnersOrder) {
                onVictoryEffect()
                val newWinners = _uiState.value.winnersOrder + playerIndex
                val rank = newWinners.size
                val rankedPlayers = _uiState.value.players.map {
                    if (it.index == playerIndex) it.copy(rank = rank) else it
                }
                if (!turnOwner.isBot) {
                    updateCareer {
                        it.copy(
                            matchesPlayed = it.matchesPlayed + 1,
                            matchesWon = if (rank == 1) it.matchesWon + 1 else it.matchesWon,
                            coins = it.coins + if (rank == 1) 500 else 150,
                            xp = it.xp + if (rank == 1) 150 else 60
                        )
                    }
                }
                _uiState.update {
                    it.copy(
                        players = rankedPlayers,
                        winnersOrder = newWinners,
                        statusMessage = "🏆 ${updatedController.name} won Rank #$rank!"
                    )
                }
            }

            val earnsBonusTurn = (dice == 6 || capturedOpponent || reachedCrown) &&
                !_uiState.value.players[playerIndex].hasMetWinCondition(currentMode)

            if (earnsBonusTurn) {
                val bonusReason = when {
                    capturedOpponent -> "Captured Enemy! +Bonus Roll!"
                    reachedCrown -> "Crown Reached! +Bonus Roll!"
                    else -> "Rolled a 6! +Bonus Roll!"
                }
                _uiState.update {
                    it.copy(
                        isRolling = false,
                        hasRolledThisTurn = false,
                        statusMessage = "${controller.name}: $bonusReason"
                    )
                }
                triggerBotIfNeeded(onDiceEffect, onBonusSixEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
            } else {
                _uiState.update { it.copy(isRolling = false) }
                advanceToNextTurn(onDiceEffect, onBonusSixEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
            }
        }
    }

    private fun areAllies(p1: Int, p2: Int, mode: GameMode): Boolean {
        if (mode != GameMode.TEAM_UP) return false
        return (p1 % 2) == (p2 % 2)
    }

    private fun updateTokenProgress(playerIndex: Int, tokenId: Int, newProgress: Int) {
        _uiState.update { state ->
            val updatedPlayers = state.players.map { p ->
                if (p.index != playerIndex) p
                else p.copy(
                    tokens = p.tokens.map { t ->
                        if (t.id == tokenId) t.copy(progress = newProgress) else t
                    }
                )
            }
            state.copy(players = updatedPlayers)
        }
    }

    private fun toGlobalTrackPosition(playerIndex: Int, relativeProgress: Int, totalPlayers: Int): Int {
        val ringSize = 52
        val playerOffset = (playerIndex * (ringSize / totalPlayers.coerceAtLeast(2))) % ringSize
        return (playerOffset + relativeProgress) % ringSize
    }

    private fun checkAndExecuteCapture(activePlayerIndex: Int, targetRelativeProgress: Int): Boolean {
        if (targetRelativeProgress in safeStepOffsets) return false

        val state = _uiState.value
        val totalPlayers = state.playerCount
        val activeGlobalSlot = toGlobalTrackPosition(activePlayerIndex, targetRelativeProgress, totalPlayers)

        var anyCaptured = false
        val updatedPlayers = state.players.map { opponent ->
            if (opponent.index == activePlayerIndex || areAllies(activePlayerIndex, opponent.index, state.gameMode)) {
                opponent
            } else {
                val opponentsOnSlot = opponent.tokens.filter { t ->
                    t.progress in 0..50 &&
                        t.progress !in safeStepOffsets &&
                        toGlobalTrackPosition(opponent.index, t.progress, totalPlayers) == activeGlobalSlot
                }
                if (opponentsOnSlot.size == 1) {
                    anyCaptured = true
                    val victimId = opponentsOnSlot.first().id
                    opponent.copy(
                        tokens = opponent.tokens.map { t ->
                            if (t.id == victimId) t.copy(progress = -1) else t
                        }
                    )
                } else {
                    opponent
                }
            }
        }.map { p ->
            if (anyCaptured && p.index == activePlayerIndex) p.copy(hasCapturedEnemy = true) else p
        }

        if (anyCaptured) {
            _uiState.update { it.copy(players = updatedPlayers) }
        }
        return anyCaptured
    }

    private fun advanceToNextTurn(
        onDiceEffect: () -> Unit,
        onBonusSixEffect: () -> Unit,
        onStepEffect: (Int) -> Unit,
        onCaptureEffect: () -> Unit,
        onVictoryEffect: () -> Unit
    ) {
        val state = _uiState.value
        val mode = state.gameMode

        // In Quick Ludo, first winner ends the match immediately
        if (mode == GameMode.QUICK_RUSH && state.winnersOrder.isNotEmpty()) {
            val champ = state.players[state.winnersOrder.first()]
            _uiState.update {
                it.copy(
                    hasRolledThisTurn = false,
                    isRolling = false,
                    statusMessage = "🏆 ${champ.name} Won Quick Ludo!"
                )
            }
            return
        }

        val activePlayers = state.players.filter { !it.hasMetWinCondition(mode) }
        if (activePlayers.size <= 1) {
            _uiState.update {
                it.copy(
                    hasRolledThisTurn = false,
                    isRolling = false,
                    statusMessage = "Match Complete! Open Modes to start again."
                )
            }
            return
        }

        var nextIndex = (state.currentTurnIndex + 1) % state.playerCount
        var guard = 0
        while (state.players[nextIndex].hasMetWinCondition(mode) &&
            resolveControllingPlayer(state.players[nextIndex], state).hasMetWinCondition(mode) &&
            guard < state.playerCount
        ) {
            nextIndex = (nextIndex + 1) % state.playerCount
            guard++
        }

        val nextPlayer = state.players[nextIndex]
        _uiState.update {
            it.copy(
                currentTurnIndex = nextIndex,
                hasRolledThisTurn = false,
                isRolling = false,
                consecutiveSixes = 0,
                statusMessage = "${nextPlayer.name}'s Turn"
            )
        }
        triggerBotIfNeeded(onDiceEffect, onBonusSixEffect, onStepEffect, onCaptureEffect, onVictoryEffect)
    }

    private fun triggerBotIfNeeded(
        onDiceEffect: () -> Unit,
        onBonusSixEffect: () -> Unit,
        onStepEffect: (Int) -> Unit,
        onCaptureEffect: () -> Unit,
        onVictoryEffect: () -> Unit
    ) {
        botJob?.cancel()
        val state = _uiState.value
        val currentPlayer = state.players.getOrNull(state.currentTurnIndex) ?: return
        if (!currentPlayer.isBot || state.hasRolledThisTurn || state.isRolling) return

        botJob = viewModelScope.launch(exceptionHandler) {
            delay(520)
            rollDice(
                onDiceEffect = onDiceEffect,
                onBonusSixEffect = onBonusSixEffect,
                onStepEffect = onStepEffect,
                onCaptureEffect = onCaptureEffect,
                onVictoryEffect = onVictoryEffect
            )
        }
    }
}
