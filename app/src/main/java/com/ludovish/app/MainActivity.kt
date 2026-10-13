package com.ludovish.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludovish.app.core.CareerManager
import com.ludovish.app.core.DisplayRateManager
import com.ludovish.app.core.HapticEngine
import com.ludovish.app.core.SoundEngine
import com.ludovish.app.domain.*
import com.ludovish.app.ui.board.LudovishBoardCanvas
import com.ludovish.app.ui.components.FloatingAppleDock
import com.ludovish.app.ui.components.GlassControlSheet
import com.ludovish.app.ui.components.ThreeDDiceView
import com.ludovish.app.ui.theme.LiquidAuroraBackground
import com.ludovish.app.ui.theme.liquidGlassPanel
import com.ludovish.app.viewmodel.LudovishViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LudovishViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DisplayRateManager.enableHighRefreshRate(this)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.collectAsState()
            val context = LocalContext.current
            val haptics = remember { HapticEngine(context) }
            val soundEngine = remember { SoundEngine(context) }
            val careerManager = remember { CareerManager(context) }

            LaunchedEffect(Unit) {
                viewModel.attachCareerManager(careerManager)
            }

            DisposableEffect(Unit) {
                onDispose {
                    soundEngine.release()
                }
            }

            val diceEffect = {
                haptics.diceRollBurst()
                soundEngine.playDiceRoll()
            }
            val bonusSixEffect = {
                soundEngine.playBonusSix()
            }
            val stepEffect: (Int) -> Unit = { stepIdx ->
                haptics.tokenHop()
                soundEngine.playTokenHop(stepOffset = stepIdx)
            }
            val captureEffect = {
                haptics.captureImpact()
                soundEngine.playCapture()
            }
            val victoryEffect = {
                haptics.victoryFanfare()
                soundEngine.playVictory()
            }

            LiquidAuroraBackground(
                palette = state.selectedPalette,
                activeAccent = state.activeAccentColor,
                glassValue = state.glassValue
            ) {
                LudovishMainScreen(
                    state = state,
                    onRollDice = {
                        viewModel.rollDice(
                            onDiceEffect = diceEffect,
                            onBonusSixEffect = bonusSixEffect,
                            onStepEffect = stepEffect,
                            onCaptureEffect = captureEffect,
                            onVictoryEffect = victoryEffect
                        )
                    },
                    onTokenTap = { tokenId ->
                        viewModel.moveToken(
                            tokenId = tokenId,
                            onDiceEffect = diceEffect,
                            onBonusSixEffect = bonusSixEffect,
                            onStepEffect = stepEffect,
                            onCaptureEffect = captureEffect,
                            onVictoryEffect = victoryEffect
                        )
                    },
                    onUndoMove = {
                        if (viewModel.undoLastMove()) {
                            haptics.captureImpact()
                            soundEngine.playBonusSix()
                        }
                    },
                    onSelectPlayerCount = { count ->
                        haptics.tick()
                        soundEngine.playGlassTick()
                        viewModel.setPlayerCount(count)
                    },
                    onSelectGameMode = { mode ->
                        haptics.tick()
                        soundEngine.playBonusSix()
                        viewModel.selectGameMode(mode)
                    },
                    onSelectTokenStyle = { style ->
                        haptics.tick()
                        soundEngine.playGlassTick()
                        viewModel.selectTokenStyle(style)
                    },
                    onSelectDiceStyle = { style ->
                        haptics.tick()
                        soundEngine.playGlassTick()
                        viewModel.selectDiceStyle(style)
                    },
                    onSelectBotDifficulty = { diff ->
                        haptics.tick()
                        soundEngine.playGlassTick()
                        viewModel.selectBotDifficulty(diff)
                    },
                    onToggleSeatBot = { index ->
                        haptics.tick()
                        soundEngine.playGlassTick()
                        viewModel.togglePlayerBotStatus(
                            playerIndex = index,
                            onDiceEffect = diceEffect,
                            onBonusSixEffect = bonusSixEffect,
                            onStepEffect = stepEffect,
                            onCaptureEffect = captureEffect,
                            onVictoryEffect = victoryEffect
                        )
                    },
                    onGlassChange = { value ->
                        haptics.tick()
                        soundEngine.playGlassTick(pan = (value - 0.5f) * 1.4f)
                        viewModel.updateGlassSlider(value)
                    },
                    onSelectPalette = { palette ->
                        haptics.tick()
                        soundEngine.playGlassTick()
                        viewModel.selectPalette(palette)
                    },
                    onCustomHueChange = { hue ->
                        viewModel.updateCustomHue(hue)
                    },
                    onSelectDock = { section ->
                        haptics.tick()
                        soundEngine.playGlassTick()
                        viewModel.selectDockSection(section)
                    },
                    onToggleSound = {
                        haptics.tick()
                        val enabled = viewModel.toggleSoundEnabled()
                        soundEngine.isEnabled = enabled
                        if (enabled) soundEngine.playBonusSix()
                    },
                    onSoundVolumeChange = { vol ->
                        soundEngine.masterVolume = vol
                        viewModel.updateSoundVolume(vol)
                    },
                    onRestartMatch = {
                        haptics.captureImpact()
                        soundEngine.playBonusSix()
                        viewModel.startNewMatch()
                    }
                )
            }
        }
    }
}

@Composable
private fun LudovishMainScreen(
    state: GameUiState,
    onRollDice: () -> Unit,
    onTokenTap: (Int) -> Unit,
    onUndoMove: () -> Unit,
    onSelectPlayerCount: (Int) -> Unit,
    onSelectGameMode: (GameMode) -> Unit,
    onSelectTokenStyle: (TokenStyle) -> Unit,
    onSelectDiceStyle: (DiceStyle) -> Unit,
    onSelectBotDifficulty: (BotDifficulty) -> Unit,
    onToggleSeatBot: (Int) -> Unit,
    onGlassChange: (Float) -> Unit,
    onSelectPalette: (ThemePalette) -> Unit,
    onCustomHueChange: (Float) -> Unit,
    onSelectDock: (DockSection) -> Unit,
    onToggleSound: () -> Unit,
    onSoundVolumeChange: (Float) -> Unit,
    onRestartMatch: () -> Unit
) {
    val activePlayer = state.players.getOrNull(state.currentTurnIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Offline Career Header + Live Player Roster
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .liquidGlassPanel(
                    glassAmount = state.glassValue,
                    accentColor = state.activeAccentColor,
                    cornerRadius = 26.dp
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LUDOVISH",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.8.sp
                    )
                    Text(
                        text = "${state.gameMode.title} • ${state.playerCount}P • ${state.tokenStyle.title}",
                        color = state.activeAccentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Offline Ludo King Coins, Level & Wins Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🪙 ${state.careerStats.coins}",
                        color = Color(0xFFFFD60A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "⭐ Lv.${state.careerStats.level} (${state.careerStats.matchesWon}W)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.players) { player ->
                    val isTurn = player.index == state.currentTurnIndex
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isTurn) player.color.copy(alpha = 0.32f)
                                else Color.White.copy(alpha = 0.06f)
                            )
                            .border(
                                width = if (isTurn) 1.5.dp else 0.8.dp,
                                color = if (isTurn) player.color else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onToggleSeatBot(player.index) }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(player.color)
                        )
                        val roleBadge = if (player.isBot) "🤖" else "👤"
                        val lockBadge = if (state.gameMode == GameMode.MASTER && !player.hasCapturedEnemy) "🔒" else ""
                        Text(
                            text = if (player.rank != null) "🏆#${player.rank} ${player.name}"
                            else "$roleBadge ${player.name} $lockBadge",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isTurn) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // 2. Interactive 120Hz Board
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            LudovishBoardCanvas(
                state = state,
                onTokenTapped = onTokenTap
            )
        }

        // 3. Multi-Tab Offline Studio Sheet
        GlassControlSheet(
            state = state,
            onPlayerCountSelect = onSelectPlayerCount,
            onSelectGameMode = onSelectGameMode,
            onSelectTokenStyle = onSelectTokenStyle,
            onSelectDiceStyle = onSelectDiceStyle,
            onSelectBotDifficulty = onSelectBotDifficulty,
            onToggleSeatBot = onToggleSeatBot,
            onGlassSliderChange = onGlassChange,
            onSelectPalette = onSelectPalette,
            onCustomHueChange = onCustomHueChange,
            onToggleSound = onToggleSound,
            onSoundVolumeChange = onSoundVolumeChange,
            onRestartMatch = onRestartMatch
        )

        // 4. Dice Controller Bar + Undo Button + Floating Apple Dock
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .liquidGlassPanel(
                        glassAmount = state.glassValue,
                        accentColor = activePlayer?.color ?: state.activeAccentColor,
                        cornerRadius = 26.dp
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${activePlayer?.name ?: "Player"} ${if (activePlayer?.isBot == true) "(AI)" else ""}",
                        color = activePlayer?.color ?: Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = state.statusMessage,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }

                if (state.canUndo && state.careerStats.coins >= 100) {
                    Box(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(1.dp, state.activeAccentColor, RoundedCornerShape(14.dp))
                            .clickable { onUndoMove() }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "⏪ Undo",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                ThreeDDiceView(
                    diceValue = state.diceValue,
                    diceStyle = state.diceStyle,
                    isRolling = state.isRolling,
                    playerColor = activePlayer?.color ?: state.activeAccentColor,
                    glassValue = state.glassValue,
                    onRollClick = onRollDice
                )
            }

            FloatingAppleDock(
                selectedSection = state.activeSheet,
                glassValue = state.glassValue,
                accentColor = state.activeAccentColor,
                onSelectSection = onSelectDock
            )
        }
    }
}
