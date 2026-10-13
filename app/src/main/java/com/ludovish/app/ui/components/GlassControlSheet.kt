package com.ludovish.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludovish.app.domain.*
import com.ludovish.app.ui.theme.liquidGlassPanel
import kotlin.math.*

@Composable
fun LudovishModalHost(
    state: GameUiState,
    onCloseModal: () -> Unit,
    onOpenModal: (ActiveModal) -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleVibration: () -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onGlassChange: (Float) -> Unit,
    onPrimaryHueChange: (Float) -> Unit,
    onAccentColorChange: (Color) -> Unit,
    onSelectTokenStyle: (TokenStyle) -> Unit,
    onSelectGameMode: (GameMode) -> Unit,
    onProceedToLobby: () -> Unit,
    onSetPlayerCount: (Int) -> Unit,
    onUpdateSlotName: (Int, String) -> Unit,
    onCycleSlotColor: (Int) -> Unit,
    onToggleSlotBot: (Int) -> Unit,
    onToggleFriendlyKill: () -> Unit,
    onRequestPlay: (Boolean) -> Unit,
    onConfirmOneTokenOut: () -> Unit,
    onResumeSavedGame: (SavedGameRecord) -> Unit,
    onDeleteSavedGame: (String) -> Unit,
    onConfirmSaveAndExit: () -> Unit,
    onExitWithoutSaving: () -> Unit,
    onSaveProfile: (String, String, String, Int) -> Unit
) {
    if (state.activeModal == ActiveModal.NONE) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        when (state.activeModal) {
            ActiveModal.SETTINGS -> SettingsModalCard(
                state = state,
                onClose = onCloseModal,
                onToggleSound = onToggleSound,
                onToggleMusic = onToggleMusic,
                onToggleVibration = onToggleVibration,
                onSetThemeMode = onSetThemeMode,
                onGlassChange = onGlassChange,
                onOpenThemeWheel = { onOpenModal(ActiveModal.THEME_COLOR_WHEEL) },
                onOpenAccentWheel = { onOpenModal(ActiveModal.ACCENT_COLOR_WHEEL) }
            )

            ActiveModal.THEME_COLOR_WHEEL -> CircularColorPickerModal(
                title = "CHOOSE THEME COLOR",
                state = state,
                isAccentPicker = false,
                onHueSelected = onPrimaryHueChange,
                onAccentSelected = onAccentColorChange,
                onClose = { onOpenModal(ActiveModal.SETTINGS) }
            )

            ActiveModal.ACCENT_COLOR_WHEEL -> CircularColorPickerModal(
                title = "CHOOSE ACCENT COLOR",
                state = state,
                isAccentPicker = true,
                onHueSelected = onPrimaryHueChange,
                onAccentSelected = onAccentColorChange,
                onClose = { onOpenModal(ActiveModal.SETTINGS) }
            )

            ActiveModal.STATISTICS -> StatisticsModalCard(
                state = state,
                onEditProfile = { onOpenModal(ActiveModal.EDIT_PROFILE) },
                onClose = onCloseModal
            )

            ActiveModal.EDIT_PROFILE -> EditProfileModalCard(
                state = state,
                onSave = { name, status, country, avatar ->
                    onSaveProfile(name, status, country, avatar)
                    onCloseModal()
                },
                onClose = onCloseModal
            )

            ActiveModal.LUDO_MODE_SELECT -> LudoModeSelectModal(
                state = state,
                onSelectToken = onSelectTokenStyle,
                onSelectMode = onSelectGameMode,
                onNext = onProceedToLobby,
                onClose = onCloseModal
            )

            ActiveModal.LUDO_PLAYER_SETUP -> LudoPlayerSetupModal(
                state = state,
                onLoadSavedGames = { onOpenModal(ActiveModal.LOAD_SAVED_GAMES) },
                onSetPlayerCount = onSetPlayerCount,
                onUpdateSlotName = onUpdateSlotName,
                onCycleSlotColor = onCycleSlotColor,
                onToggleSlotBot = onToggleSlotBot,
                onToggleFriendlyKill = onToggleFriendlyKill,
                onPlayStandard = { onRequestPlay(false) },
                onPlayOneTokenOut = { onRequestPlay(true) },
                onBack = { onOpenModal(ActiveModal.LUDO_MODE_SELECT) }
            )

            ActiveModal.KEEP_ONE_TOKEN_OUT_PROMPT -> KeepOneTokenOutModal(
                state = state,
                onOkay = onConfirmOneTokenOut,
                onClose = { onOpenModal(ActiveModal.LUDO_PLAYER_SETUP) }
            )

            ActiveModal.LOAD_SAVED_GAMES -> LoadSavedGamesModal(
                state = state,
                onResume = onResumeSavedGame,
                onDelete = onDeleteSavedGame,
                onClose = onCloseModal
            )

            ActiveModal.IN_GAME_QUIT -> QuitGameModal(
                state = state,
                onYesQuit = {
                    if (state.currentScreen == AppScreen.LUDO_GAMEPLAY && state.gameMode != GameMode.RUSH_MODE) {
                        onOpenModal(ActiveModal.IN_GAME_SAVE_PROMPT)
                    } else {
                        onExitWithoutSaving()
                    }
                },
                onNoResume = onCloseModal,
                onToggleSound = onToggleSound,
                onToggleVibration = onToggleVibration,
                onToggleMusic = onToggleMusic
            )

            ActiveModal.IN_GAME_SAVE_PROMPT -> SaveGamePromptModal(
                state = state,
                onYesSave = onConfirmSaveAndExit,
                onNoExit = onExitWithoutSaving
            )

            else -> onCloseModal()
        }
    }
}

@Composable
private fun SettingsModalCard(
    state: GameUiState,
    onClose: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleVibration: () -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onGlassChange: (Float) -> Unit,
    onOpenThemeWheel: () -> Unit,
    onOpenAccentWheel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.90f)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.customAccentColor,
                cornerRadius = 26.dp
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Settings",
            color = Color(0xFFFFD60A),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        SettingRow("Audio") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldCircleButton(text = if (state.soundEnabled) "🔊" else "🔇", active = state.soundEnabled, onClick = onToggleSound)
                GoldCircleButton(text = if (state.musicEnabled) "🎵" else "🚫", active = state.musicEnabled, onClick = onToggleMusic)
            }
        }

        SettingRow("Vibration") {
            GoldCircleButton(text = if (state.vibrationEnabled) "📳" else "📴", active = state.vibrationEnabled, onClick = onToggleVibration)
        }

        SettingRow("App Mode") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppThemeMode.entries.forEach { mode ->
                    PillActionButton(
                        text = mode.name,
                        selected = state.themeMode == mode,
                        onClick = { onSetThemeMode(mode) }
                    )
                }
            }
        }

        SettingRow("Theme & Accent") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillActionButton(text = "Theme Wheel", selected = true, onClick = onOpenThemeWheel)
                PillActionButton(text = "Accent Wheel", selected = true, onClick = onOpenAccentWheel)
            }
        }

        // Dynamic Liquid Glass Slider (Clear -> Frosted -> Opaque)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Liquid Glass Effect", color = Color(0xFFFFD60A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "${state.glassModeLabel.label} (${(state.glassValue * 100).toInt()}%)", color = Color.White, fontSize = 12.sp)
            }
            Slider(
                value = state.glassValue,
                onValueChange = onGlassChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFD60A),
                    activeTrackColor = state.customAccentColor
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Clear", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                Text("Frosted", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                Text("Opaque", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
            }
        }

        SettingRow("Language") {
            PillActionButton(text = "English ▼", selected = true, onClick = {})
        }

        SettingRow("Random Dice Certificate") {
            PillActionButton(text = "Verified 100% Fair", selected = false, onClick = {})
        }

        Spacer(modifier = Modifier.height(4.dp))
        GoldenActionBtn(text = "BACK TO HOME", onClick = onClose, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun CircularColorPickerModal(
    title: String,
    state: GameUiState,
    isAccentPicker: Boolean,
    onHueSelected: (Float) -> Unit,
    onAccentSelected: (Color) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.customAccentColor,
                cornerRadius = 28.dp
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = title, color = Color(0xFFFFD60A), fontSize = 18.sp, fontWeight = FontWeight.Black)

        // Interactive 360-degree Circular HSV Color Wheel
        Canvas(
            modifier = Modifier
                .size(210.dp)
                .pointerInput(isAccentPicker) {
                    detectTapGestures { offset ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val angle = ((atan2(offset.y - cy, offset.x - cx) * 180.0 / PI) + 360.0) % 360.0
                        if (isAccentPicker) {
                            onAccentSelected(Color.hsv(angle.toFloat(), 0.9f, 1f))
                        } else {
                            onHueSelected(angle.toFloat())
                        }
                    }
                }
        ) {
            val sweepColors = List(13) { i -> Color.hsv((i * 30f) % 360f, 0.9f, 1f) }
            drawCircle(
                brush = Brush.sweepGradient(sweepColors),
                radius = size.minDimension / 2f
            )
            drawCircle(
                color = Color.White,
                radius = size.minDimension / 2f,
                style = Stroke(width = 6f)
            )
        }

        if (isAccentPicker) {
            Text(text = "5 Preset Vibrant Accent Bubbles", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PresetAccentBubbles.forEach { bubbleColor ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(bubbleColor)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { onAccentSelected(bubbleColor) }
                    )
                }
            }
        }

        GoldenActionBtn(text = "APPLY COLOR", onClick = onClose, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun StatisticsModalCard(
    state: GameUiState,
    onEditProfile: () -> Unit,
    onClose: () -> Unit
) {
    val stats = state.careerStats
    Column(
        modifier = Modifier
            .fillMaxWidth(0.90f)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.customAccentColor,
                cornerRadius = 26.dp
            )
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD90429))
                .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
            Text(text = "STATISTICS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E3A8A).copy(alpha = 0.65f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color(0xFFFFD60A), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = AvatarEmojis[stats.avatarIndex % AvatarEmojis.size], fontSize = 34.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${stats.playerName} (${stats.country})", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text(text = "🪙 ${stats.coins}   💎 ${stats.gems}", color = Color(0xFFFFD60A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = "Level ${stats.level} • ${stats.xpInCurrentLevel}/180 XP", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
            }
        }

        val statRows = listOf(
            "👍 GAMES WON" to "${stats.gamesWon}",
            "👎 GAMES LOST" to "${stats.gamesLost}",
            "⚡ WIN STREAK" to "${stats.winStreak}",
            "🎯 TOKENS CAPTURED" to "${stats.tokensCaptured}",
            "💥 TOKENS KILLED" to "${stats.tokensKilled}",
            "🏅 PERFORMANCE RATING" to "${1000 + stats.gamesWon * 25}",
            "🏆 TOURNAMENTS WON" to "${stats.tournamentsWon}"
        )
        statRows.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A192F).copy(alpha = 0.75f))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = value, color = Color(0xFFFFD60A), fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GoldenActionBtn(text = "Edit Profile", onClick = onEditProfile, modifier = Modifier.weight(1f))
            GoldenActionBtn(text = "Close", onClick = onClose, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun EditProfileModalCard(
    state: GameUiState,
    onSave: (String, String, String, Int) -> Unit,
    onClose: () -> Unit
) {
    var name by remember { mutableStateOf(state.careerStats.playerName) }
    var status by remember { mutableStateOf(state.careerStats.statusText) }
    var countryIdx by remember { mutableStateOf(CountryList.indexOf(state.careerStats.country).coerceAtLeast(0)) }
    var avatarIdx by remember { mutableStateOf(state.careerStats.avatarIndex) }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.customAccentColor,
                cornerRadius = 26.dp
            )
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "EDIT PROFILE",
            color = Color(0xFFFFD60A),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )

        // Country selector bar (matches video 01:19)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A192F))
                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable { countryIdx = (countryIdx + 1) % CountryList.size }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Country: ${CountryList[countryIdx]}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "▼", color = Color(0xFFFFD60A), fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(16) },
            label = { Text("Player Name", color = Color.White.copy(alpha = 0.7f)) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF0A192F),
                unfocusedContainerColor = Color(0xFF0A192F)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "SELECT PROFILE PICTURE", color = Color(0xFFFFD60A), fontSize = 13.sp, fontWeight = FontWeight.Black)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AvatarEmojis.indices.toList()) { idx ->
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (avatarIdx == idx) Color(0xFF2563EB) else Color(0xFF0F172A))
                        .border(
                            width = if (avatarIdx == idx) 2.5.dp else 1.dp,
                            color = if (avatarIdx == idx) Color(0xFFFFD60A) else Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { avatarIdx = idx },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = AvatarEmojis[idx], fontSize = 28.sp)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GoldenActionBtn(text = "Continue", onClick = { onSave(name, status, CountryList[countryIdx], avatarIdx) }, modifier = Modifier.weight(1f))
            GoldenActionBtn(text = "Cancel", onClick = onClose, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LudoModeSelectModal(
    state: GameUiState,
    onSelectToken: (TokenStyle) -> Unit,
    onSelectMode: (GameMode) -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.90f)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.customAccentColor,
                cornerRadius = 26.dp
            )
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "SELECT TOKEN", color = Color(0xFFFFD60A), fontSize = 15.sp, fontWeight = FontWeight.Black)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(TokenStyle.entries) { style ->
                val selected = state.tokenStyle == style
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) Color(0xFF2563EB) else Color(0xFF0A192F))
                        .border(1.5.dp, if (selected) Color(0xFFFFD60A) else Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .clickable { onSelectToken(style) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = style.badgeSymbol, fontSize = 20.sp)
                    Text(text = style.title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(text = "SELECT GAME", color = Color(0xFFFFD60A), fontSize = 15.sp, fontWeight = FontWeight.Black)

        GameMode.entries.forEach { mode ->
            val selected = state.gameMode == mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) Color(0xFF1D4ED8) else Color(0xFF0A192F))
                    .border(2.dp, if (selected) Color(0xFFFFD60A) else Color(0xFF3B82F6), RoundedCornerShape(18.dp))
                    .clickable { onSelectMode(mode) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = mode.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    Text(text = mode.subtitle, color = Color.White.copy(alpha = 0.78f), fontSize = 11.sp)
                }
                Text(text = if (selected) "✅" else "⭕", fontSize = 18.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GoldenActionBtn(text = "Back", onClick = onClose, modifier = Modifier.weight(1f))
            GoldenActionBtn(text = "Next", onClick = onNext, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LudoPlayerSetupModal(
    state: GameUiState,
    onLoadSavedGames: () -> Unit,
    onSetPlayerCount: (Int) -> Unit,
    onUpdateSlotName: (Int, String) -> Unit,
    onCycleSlotColor: (Int) -> Unit,
    onToggleSlotBot: (Int) -> Unit,
    onToggleFriendlyKill: () -> Unit,
    onPlayStandard: () -> Unit,
    onPlayOneTokenOut: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.customAccentColor,
                cornerRadius = 26.dp
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GoldenActionBtn(text = "Load Saved Games", onClick = onLoadSavedGames)

        Text(text = "CHOOSE COLOR AND NAME", color = Color(0xFFFFD60A), fontSize = 14.sp, fontWeight = FontWeight.Black)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 230.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.lobbySlots.take(state.playerCount)) { slot ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A192F).copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.gameMode == GameMode.TEAM_UP) {
                        Text(
                            text = if (slot.slotIndex % 2 == 0) "TEAM A" else "TEAM B",
                            color = if (slot.slotIndex % 2 == 0) Color(0xFF38BDF8) else Color(0xFFFF4D6D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Tap to cycle color
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(TenPlayerColors[slot.colorIndex % TenPlayerColors.size])
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { onCycleSlotColor(slot.slotIndex) }
                    )

                    // Name box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = slot.name,
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Bot Toggle Button (matches video 03:03)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (slot.isBot) Color(0xFF2563EB) else Color(0xFF1E293B))
                            .border(1.dp, Color(0xFFFFD60A), RoundedCornerShape(8.dp))
                            .clickable { onToggleSlotBot(slot.slotIndex) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (slot.isBot) "🤖" else "👤", fontSize = 15.sp)
                    }
                }
            }
        }

        if (state.gameMode == GameMode.TEAM_UP) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleFriendlyKill() }
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = if (state.friendlyKillEnabled) "☑️" else "⬜", fontSize = 16.sp)
                Text(text = "Friendly Kill", color = Color(0xFFFFD60A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Player Count Selector Pills: 2P, 3P, 4P, 5P, 6P, 7P, 8P, 9P, 10P
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items((2..10).toList()) { cnt ->
                val selected = state.playerCount == cnt
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selected) Color(0xFF2563EB) else Color(0xFF0A192F))
                        .border(1.5.dp, if (selected) Color(0xFFFFD60A) else Color(0xFF3B82F6), RoundedCornerShape(18.dp))
                        .clickable { onSetPlayerCount(cnt) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(text = "${cnt}P", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }

        GoldenActionBtn(text = "Play", onClick = onPlayStandard, modifier = Modifier.fillMaxWidth(0.75f))
        GoldenActionBtn(text = "Play\n- One Token Out -", onClick = onPlayOneTokenOut, modifier = Modifier.fillMaxWidth(0.75f))
        PillActionButton(text = "◀ Back", selected = false, onClick = onBack)
    }
}

@Composable
private fun KeepOneTokenOutModal(
    state: GameUiState,
    onOkay: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.86f)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.customAccentColor,
                cornerRadius = 24.dp
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Keep one token out for all!",
            color = Color(0xFF2DC653),
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Every player starts the match with 1 token already deployed on their starting square!",
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GoldenActionBtn(text = "OKAY", onClick = onOkay, modifier = Modifier.weight(1f))
            GoldenActionBtn(text = "CANCEL", onClick = onClose, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LoadSavedGamesModal(
    state: GameUiState,
    onResume: (SavedGameRecord) -> Unit,
    onDelete: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.customAccentColor,
                cornerRadius = 26.dp
            )
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFBE0B))
                .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
            Text(text = "LOAD GAMES", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        LazyColumn(
            modifier = Modifier.heightIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.savedGames) { rec ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0A192F))
                        .border(1.5.dp, Color(0xFFFFD60A), RoundedCornerShape(14.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD90429))
                            .clickable { onDelete(rec.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✖", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(text = rec.timestamp, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        Text(
                            text = "Game Type: ${rec.playerCount} Players (${rec.gameModeName})",
                            color = Color(0xFFFFD60A),
                            fontSize = 11.sp
                        )
                        Text(
                            text = rec.playerNames.joinToString(" • "),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2DC653))
                            .clickable { onResume(rec) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "▶", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        GoldenActionBtn(text = "CLOSE", onClick = onClose, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun QuitGameModal(
    state: GameUiState,
    onYesQuit: () -> Unit,
    onNoResume: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onToggleMusic: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.customAccentColor,
                cornerRadius = 24.dp
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "QUIT GAME?", color = Color(0xFFFFD60A), fontSize = 22.sp, fontWeight = FontWeight.Black)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoldenActionBtn(text = "Yes", onClick = onYesQuit)
            GoldCircleButton(text = if (state.soundEnabled) "🔊" else "🔇", active = state.soundEnabled, onClick = onToggleSound)
            GoldCircleButton(text = if (state.vibrationEnabled) "📳" else "📴", active = state.vibrationEnabled, onClick = onToggleVibration)
            GoldCircleButton(text = if (state.musicEnabled) "🎵" else "🚫", active = state.musicEnabled, onClick = onToggleMusic)
            GoldenActionBtn(text = "No", onClick = onNoResume)
        }
    }
}

@Composable
private fun SaveGamePromptModal(
    state: GameUiState,
    onYesSave: () -> Unit,
    onNoExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.86f)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.customAccentColor,
                cornerRadius = 24.dp
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "SAVE GAME", color = Color(0xFFFFD60A), fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(text = "Do you want to save game?", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.saveTimestampPreview,
                color = Color.DarkGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GoldenActionBtn(text = "Yes", onClick = onYesSave, modifier = Modifier.weight(1f).padding(end = 6.dp))
            GoldenActionBtn(text = "No", onClick = onNoExit, modifier = Modifier.weight(1f).padding(start = 6.dp))
        }
    }
}

@Composable
private fun SettingRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFFFFD60A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun GoldCircleButton(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (active) Color(0xFF1D4ED8) else Color(0xFF64748B))
            .border(1.8.dp, Color(0xFFFFD60A), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}

@Composable
private fun PillActionButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0xFF1D4ED8) else Color(0xFF0A192F))
            .border(1.5.dp, Color(0xFFFFD60A), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GoldenActionBtn(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E40AF), Color(0xFF0F172A))
                )
            )
            .border(2.dp, Color(0xFFFFBE0B), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}
