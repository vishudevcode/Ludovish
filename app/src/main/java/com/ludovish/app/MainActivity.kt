package com.ludovish.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludovish.app.core.CareerManager
import com.ludovish.app.core.DisplayRateManager
import com.ludovish.app.core.HapticEngine
import com.ludovish.app.core.SoundEngine
import com.ludovish.app.domain.*
import com.ludovish.app.ui.board.LudovishBoardCanvas
import com.ludovish.app.ui.board.SnakesAndLaddersCanvas
import com.ludovish.app.ui.components.*
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
                onDispose { soundEngine.release() }
            }

            val diceEffect = {
                if (state.vibrationEnabled) haptics.diceRollBurst()
                soundEngine.playDiceRoll()
            }
            val stepEffect: (Int) -> Unit = { idx ->
                if (state.vibrationEnabled) haptics.tokenHop()
                soundEngine.playTokenHop(stepOffset = idx)
            }
            val captureEffect = {
                if (state.vibrationEnabled) haptics.captureImpact()
                soundEngine.playCapture()
            }
            val victoryEffect = {
                if (state.vibrationEnabled) haptics.victoryFanfare()
                soundEngine.playVictory()
            }

            LiquidAuroraBackground(
                primaryColor = state.primaryThemeColor,
                accentColor = state.customAccentColor,
                glassValue = state.glassValue,
                themeMode = state.themeMode,
                isSnakesMode = state.currentScreen in listOf(
                    AppScreen.SNAKES_HOME,
                    AppScreen.SNAKES_THEME_SELECT,
                    AppScreen.SNAKES_BOARD_SELECT,
                    AppScreen.SNAKES_GAMEPLAY
                )
            ) {
                Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                    when (state.currentScreen) {
                        AppScreen.SPLASH -> KingDiceSplashScreen(onTapSkip = { viewModel.skipSplash() })

                        AppScreen.LUDO_HOME -> LudoHomeDashboard(
                            state = state,
                            onProfileClick = { viewModel.openModal(ActiveModal.STATISTICS) },
                            onSettingsClick = { viewModel.openModal(ActiveModal.SETTINGS) },
                            onOpenHubMode = { type ->
                                soundEngine.playGlassTick()
                                viewModel.openLudoSetupForHub(type)
                            },
                            onSwitchToSnakes = {
                                soundEngine.playBonusSix()
                                viewModel.switchMainHub(toSnakes = true)
                            },
                            onSpinReward = {
                                soundEngine.playVictory()
                                viewModel.spinDailyWheelReward()
                            },
                            onSelectBottomTab = { tab ->
                                soundEngine.playGlassTick()
                                viewModel.selectBottomTab(tab)
                            }
                        )

                        AppScreen.SNAKES_HOME -> SnakesHomeDashboard(
                            state = state,
                            onProfileClick = { viewModel.openModal(ActiveModal.STATISTICS) },
                            onSettingsClick = { viewModel.openModal(ActiveModal.SETTINGS) },
                            onStartComputer = { viewModel.startSnakesMatch(vsComputer = true) },
                            onStartPassNPlay = { viewModel.startSnakesMatch(vsComputer = false) },
                            onOpenThemes = { viewModel.openSnakesThemeSelector() },
                            onOpenBoards = { viewModel.openSnakesBoardSelector() },
                            onSwitchToLudo = {
                                soundEngine.playBonusSix()
                                viewModel.switchMainHub(toSnakes = false)
                            },
                            onSelectBottomTab = { viewModel.selectBottomTab(it) }
                        )

                        AppScreen.SNAKES_THEME_SELECT -> SnakesThemeSelectorScreen(
                            state = state,
                            onPrev = { viewModel.cycleSnakesTheme(-1) },
                            onNext = { viewModel.cycleSnakesTheme(1) },
                            onSelect = { viewModel.switchMainHub(toSnakes = true) }
                        )

                        AppScreen.SNAKES_BOARD_SELECT -> SnakesBoardSelectorScreen(
                            state = state,
                            onPrev = { viewModel.cycleSnakesBoard(-1) },
                            onNext = { viewModel.cycleSnakesBoard(1) },
                            onSelect = { viewModel.switchMainHub(toSnakes = true) }
                        )

                        AppScreen.LUDO_GAMEPLAY -> LudoActiveGameplayScreen(
                            state = state,
                            onRollDice = { pIdx ->
                                viewModel.rollDiceForPlayer(pIdx, diceEffect, stepEffect, captureEffect, victoryEffect)
                            },
                            onTokenTap = { pIdx, tId ->
                                viewModel.moveToken(pIdx, tId, diceEffect, stepEffect, captureEffect, victoryEffect)
                            },
                            onToggleBottomLeftMenu = { viewModel.toggleBottomLeftMenu() },
                            onOpenQuitModal = { viewModel.openModal(ActiveModal.IN_GAME_QUIT) }
                        )

                        AppScreen.SNAKES_GAMEPLAY -> SnakesActiveGameplayScreen(
                            state = state,
                            onRollDice = {
                                viewModel.rollSnakesDice(diceEffect, stepEffect, captureEffect, victoryEffect)
                            },
                            onOpenQuitModal = { viewModel.openModal(ActiveModal.IN_GAME_QUIT) }
                        )
                    }

                    LudovishModalHost(
                        state = state,
                        onCloseModal = { viewModel.closeModal() },
                        onOpenModal = { viewModel.openModal(it) },
                        onToggleSound = {
                            val en = viewModel.toggleSound()
                            soundEngine.isEnabled = en
                        },
                        onToggleMusic = { viewModel.toggleMusic() },
                        onToggleVibration = { viewModel.toggleVibration() },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onGlassChange = { viewModel.updateGlassSlider(it) },
                        onPrimaryHueChange = { viewModel.updatePrimaryThemeHue(it) },
                        onAccentColorChange = { viewModel.updateCustomAccentColor(it) },
                        onSelectTokenStyle = { viewModel.selectTokenStyle(it) },
                        onSelectGameMode = { viewModel.selectGameMode(it) },
                        onProceedToLobby = { viewModel.proceedToPlayerSetup() },
                        onSetPlayerCount = { viewModel.setLobbyPlayerCount(it) },
                        onUpdateSlotName = { idx, name -> viewModel.updateLobbySlotName(idx, name) },
                        onCycleSlotColor = { viewModel.cycleLobbySlotColor(it) },
                        onToggleSlotBot = { viewModel.toggleLobbySlotBot(it) },
                        onToggleFriendlyKill = { viewModel.toggleFriendlyKill() },
                        onRequestPlay = { oneOut -> viewModel.requestStartLudoMatch(oneOut) },
                        onConfirmOneTokenOut = { viewModel.confirmKeepOneTokenOutAndStart() },
                        onResumeSavedGame = { viewModel.resumeSavedGame(it) },
                        onDeleteSavedGame = { viewModel.deleteSavedGame(it) },
                        onConfirmSaveAndExit = { viewModel.saveCurrentGameAndExit() },
                        onExitWithoutSaving = { viewModel.exitToHome() },
                        onSaveProfile = { n, s, c, a -> viewModel.updateProfile(n, s, c, a) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KingDiceSplashScreen(onTapSkip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onTapSkip() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(Color.White)
                .border(4.dp, Color(0xFFFFD60A), RoundedCornerShape(36.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "👑", fontSize = 44.sp)
                Text(text = "🎲", fontSize = 54.sp)
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(text = "LUDOVISH KING", color = Color(0xFFFFD60A), fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        Text(text = "100% Offline • 2 to 10 Players • 120Hz Liquid Glass", color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun LudoHomeDashboard(
    state: GameUiState,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenHubMode: (HubEntryType) -> Unit,
    onSwitchToSnakes: () -> Unit,
    onSpinReward: () -> Unit,
    onSelectBottomTab: (BottomNavTab) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LudoKingTopHeader(
            stats = state.careerStats,
            glassValue = state.glassValue,
            accentColor = state.customAccentColor,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick
        )

        // Central Crown Logo Banner (matches video 00:00)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "👑", fontSize = 36.sp)
            Text(
                text = "LUDO",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Text(
                text = "LUDOVISH EDITION • 2P TO 10P",
                color = Color(0xFFFFD60A),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Mode Hub Buttons (Team Up, Computer, Pass N Play, Tournament)
        Column(
            modifier = Modifier.fillMaxWidth(0.88f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HubCardButton(
                    icon = "🖥️",
                    title = "COMPUTER",
                    glassValue = state.glassValue,
                    accent = state.customAccentColor,
                    onClick = { onOpenHubMode(HubEntryType.COMPUTER) },
                    modifier = Modifier.weight(1f)
                )
                HubCardButton(
                    icon = "👥",
                    title = "PASS N PLAY",
                    glassValue = state.glassValue,
                    accent = state.customAccentColor,
                    onClick = { onOpenHubMode(HubEntryType.PASS_N_PLAY) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HubCardButton(
                    icon = "🤝",
                    title = "TEAM UP",
                    glassValue = state.glassValue,
                    accent = state.customAccentColor,
                    onClick = { onOpenHubMode(HubEntryType.TEAM_UP) },
                    modifier = Modifier.weight(1f)
                )
                HubCardButton(
                    icon = "🏆",
                    title = "TOURNAMENT",
                    glassValue = state.glassValue,
                    accent = state.customAccentColor,
                    onClick = { onOpenHubMode(HubEntryType.TOURNAMENT) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Bottom Floating Switcher Row: Snakes & Ladders 7-Icon + Daily Free Spin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1D4ED8))
                        .border(2.dp, Color(0xFFFFD60A), RoundedCornerShape(18.dp))
                        .clickable { onSwitchToSnakes() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = "🐍 7 Snakes & Ladders", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0A192F))
                        .border(2.dp, Color(0xFFFFD60A), RoundedCornerShape(18.dp))
                        .clickable { onSpinReward() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = "🎡 FREE +1200 SPIN", color = Color(0xFFFFD60A), fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }

        FloatingAppleDock(
            selectedTab = state.bottomTab,
            glassValue = state.glassValue,
            accentColor = state.customAccentColor,
            onSelectTab = onSelectBottomTab
        )
    }
}

@Composable
private fun SnakesHomeDashboard(
    state: GameUiState,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStartComputer: () -> Unit,
    onStartPassNPlay: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenBoards: () -> Unit,
    onSwitchToLudo: () -> Unit,
    onSelectBottomTab: (BottomNavTab) -> Unit
) {
    val activeTheme = SnakesTheme.entries[state.snakesThemeIndex % SnakesTheme.entries.size]
    val activeBoard = SevenSnakesBoards[state.snakesBoardIndex % SevenSnakesBoards.size]

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LudoKingTopHeader(
            stats = state.careerStats,
            glassValue = state.glassValue,
            accentColor = state.customAccentColor,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🐍 🪜 🐍", fontSize = 32.sp)
            Text(
                text = "SNAKES AND LADDERS",
                color = Color(0xFFFFD60A),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Theme: ${activeTheme.title} • Board ${activeBoard.boardNumber}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.88f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HubCardButton(
                    icon = "🖥️",
                    title = "COMPUTER",
                    glassValue = state.glassValue,
                    accent = Color(0xFF2DC653),
                    onClick = onStartComputer,
                    modifier = Modifier.weight(1f)
                )
                HubCardButton(
                    icon = "👥",
                    title = "PASS N PLAY",
                    glassValue = state.glassValue,
                    accent = Color(0xFF2DC653),
                    onClick = onStartPassNPlay,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GoldenActionBtn(text = "🎨 Choose Theme", onClick = onOpenThemes)
                GoldenActionBtn(text = "🪜 Choose Board (1-7)", onClick = onOpenBoards)
            }

            GoldenActionBtn(
                text = "🎲 Back to Ludo King Hub",
                onClick = onSwitchToLudo,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        FloatingAppleDock(
            selectedTab = state.bottomTab,
            glassValue = state.glassValue,
            accentColor = state.customAccentColor,
            onSelectTab = onSelectBottomTab
        )
    }
}

@Composable
private fun SnakesThemeSelectorScreen(
    state: GameUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelect: () -> Unit
) {
    val theme = SnakesTheme.entries[state.snakesThemeIndex % SnakesTheme.entries.size]
    val layout = SevenSnakesBoards[state.snakesBoardIndex % SevenSnakesBoards.size]

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Choose Theme", color = Color(0xFFFFD60A), fontSize = 26.sp, fontWeight = FontWeight.Black)
        GoldenActionBtn(text = theme.title, onClick = {})

        Box(modifier = Modifier.fillMaxWidth(0.86f)) {
            SnakesAndLaddersCanvas(theme = theme, boardLayout = layout, players = emptyList())
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoldenActionBtn(text = "◀", onClick = onPrev)
            GoldenActionBtn(text = "SELECT", onClick = onSelect)
            GoldenActionBtn(text = "▶", onClick = onNext)
        }
    }
}

@Composable
private fun SnakesBoardSelectorScreen(
    state: GameUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelect: () -> Unit
) {
    val theme = SnakesTheme.entries[state.snakesThemeIndex % SnakesTheme.entries.size]
    val layout = SevenSnakesBoards[state.snakesBoardIndex % SevenSnakesBoards.size]

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Choose Board", color = Color(0xFFFFD60A), fontSize = 26.sp, fontWeight = FontWeight.Black)
        GoldenActionBtn(text = "Board ${layout.boardNumber}", onClick = {})

        Box(modifier = Modifier.fillMaxWidth(0.86f)) {
            SnakesAndLaddersCanvas(theme = theme, boardLayout = layout, players = emptyList())
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoldenActionBtn(text = "◀", onClick = onPrev)
            GoldenActionBtn(text = "SELECT", onClick = onSelect)
            GoldenActionBtn(text = "▶", onClick = onNext)
        }
    }
}

@Composable
private fun LudoActiveGameplayScreen(
    state: GameUiState,
    onRollDice: (Int) -> Unit,
    onTokenTap: (Int, Int) -> Unit,
    onToggleBottomLeftMenu: () -> Unit,
    onOpenQuitModal: () -> Unit
) {
    val isRush = state.gameMode == GameMode.RUSH_MODE

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Corner Dice Trays (for 2-4P mode: P1 Top-Left Red, P2 Top-Right Green)
            if (state.playerCount <= 4) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val pTopLeft = state.players.getOrNull(1)
                    if (pTopLeft != null) {
                        LudoCornerDiceTray(
                            playerName = pTopLeft.name,
                            playerColor = pTopLeft.color,
                            diceValue = if (isRush) pTopLeft.diceValue else state.diceValue,
                            isCurrentTurn = isRush || state.currentTurnIndex == 1,
                            isRolling = if (isRush) pTopLeft.isRolling else (state.isRolling && state.currentTurnIndex == 1),
                            alignRight = false,
                            onRollClick = { onRollDice(1) }
                        )
                    } else Spacer(modifier = Modifier.width(10.dp))

                    val pTopRight = state.players.getOrNull(2)
                    if (pTopRight != null) {
                        LudoCornerDiceTray(
                            playerName = pTopRight.name,
                            playerColor = pTopRight.color,
                            diceValue = if (isRush) pTopRight.diceValue else state.diceValue,
                            isCurrentTurn = isRush || state.currentTurnIndex == 2,
                            isRolling = if (isRush) pTopRight.isRolling else (state.isRolling && state.currentTurnIndex == 2),
                            alignRight = true,
                            onRollClick = { onRollDice(2) }
                        )
                    }
                }
            } else {
                // 5P to 10P Header Turn Banner
                val activeP = state.players.getOrNull(state.currentTurnIndex)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(activeP?.color ?: Color.Blue)
                        .border(2.dp, Color.White, RoundedCornerShape(18.dp))
                        .clickable { onRollDice(state.currentTurnIndex) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${activeP?.name ?: "Player"}'s Turn • Tap Center Dice (${state.diceValue})",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            // Center Ludo Board (4P Square or 5P-10P Polygon)
            Box(contentAlignment = Alignment.Center) {
                LudovishBoardCanvas(
                    state = state,
                    onTokenTapped = onTokenTap,
                    onCenterDiceTapped = { onRollDice(state.currentTurnIndex) }
                )

                // "GAME START" Banner Overlay (matches video 00:34 & 01:20)
                AnimatedVisibility(visible = state.showGameStartBanner) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0A192F))
                            .border(3.dp, Color(0xFFFFD60A), RoundedCornerShape(16.dp))
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GAME START",
                            color = Color(0xFFFFD60A),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            // Bottom Corner Dice Trays (for 2-4P mode: P0 Bottom-Left Blue, P3 Bottom-Right Yellow)
            if (state.playerCount <= 4) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val pBottomLeft = state.players.getOrNull(0)
                    if (pBottomLeft != null) {
                        LudoCornerDiceTray(
                            playerName = pBottomLeft.name,
                            playerColor = pBottomLeft.color,
                            diceValue = if (isRush) pBottomLeft.diceValue else state.diceValue,
                            isCurrentTurn = isRush || state.currentTurnIndex == 0,
                            isRolling = if (isRush) pBottomLeft.isRolling else (state.isRolling && state.currentTurnIndex == 0),
                            alignRight = false,
                            onRollClick = { onRollDice(0) }
                        )
                    }

                    val pBottomRight = state.players.getOrNull(3)
                    if (pBottomRight != null) {
                        LudoCornerDiceTray(
                            playerName = pBottomRight.name,
                            playerColor = pBottomRight.color,
                            diceValue = if (isRush) pBottomRight.diceValue else state.diceValue,
                            isCurrentTurn = isRush || state.currentTurnIndex == 3,
                            isRolling = if (isRush) pBottomRight.isRolling else (state.isRolling && state.currentTurnIndex == 3),
                            alignRight = true,
                            onRollClick = { onRollDice(3) }
                        )
                    }
                }
            }
        }

        // Bottom-Left Menu Button + Popup ("Exit" / "Rules" like video 00:53 & 02:23)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (state.showBottomLeftMenuPopup) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A192F))
                        .border(1.5.dp, Color(0xFFFFD60A), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✖ Exit",
                        color = Color(0xFFFFD60A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.clickable { onOpenQuitModal() }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1D4ED8))
                    .border(2.dp, Color(0xFFFFD60A), CircleShape)
                    .clickable { onToggleBottomLeftMenu() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "☰", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SnakesActiveGameplayScreen(
    state: GameUiState,
    onRollDice: () -> Unit,
    onOpenQuitModal: () -> Unit
) {
    val theme = SnakesTheme.entries[state.snakesThemeIndex % SnakesTheme.entries.size]
    val layout = SevenSnakesBoards[state.snakesBoardIndex % SevenSnakesBoards.size]
    val activePlayer = state.players.getOrNull(state.currentTurnIndex)

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoldenActionBtn(text = "✖ Quit", onClick = onOpenQuitModal)
            Text(
                text = "${theme.title} • Board ${layout.boardNumber}",
                color = Color(0xFFFFD60A),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }

        SnakesAndLaddersCanvas(
            theme = theme,
            boardLayout = layout,
            players = state.players
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassPanel(
                    glassAmount = state.glassValue,
                    accentColor = state.customAccentColor,
                    cornerRadius = 22.dp
                )
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${activePlayer?.name ?: "Player"}'s Turn",
                    color = Color(0xFFFFD60A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Square: ${activePlayer?.snakesPosition ?: 1} / 100",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            ThreeDDiceBox(
                diceValue = state.diceValue,
                isRolling = state.isRolling,
                showDice = true,
                onClick = onRollDice
            )
        }
    }
}

@Composable
private fun HubCardButton(
    icon: String,
    title: String,
    glassValue: Float,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .liquidGlassPanel(
                glassAmount = glassValue,
                accentColor = accent,
                cornerRadius = 22.dp
            )
            .clickable { onClick() }
            .padding(vertical = 18.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = icon, fontSize = 32.sp)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFBE0B))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
