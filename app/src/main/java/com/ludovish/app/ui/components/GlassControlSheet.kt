package com.ludovish.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludovish.app.domain.*
import com.ludovish.app.ui.theme.liquidGlassPanel

@Composable
fun GlassControlSheet(
    state: GameUiState,
    onPlayerCountSelect: (Int) -> Unit,
    onSelectGameMode: (GameMode) -> Unit,
    onSelectTokenStyle: (TokenStyle) -> Unit,
    onSelectDiceStyle: (DiceStyle) -> Unit,
    onSelectBotDifficulty: (BotDifficulty) -> Unit,
    onToggleSeatBot: (Int) -> Unit,
    onGlassSliderChange: (Float) -> Unit,
    onSelectPalette: (ThemePalette) -> Unit,
    onCustomHueChange: (Float) -> Unit,
    onToggleSound: () -> Unit,
    onSoundVolumeChange: (Float) -> Unit,
    onRestartMatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.activeSheet == DockSection.PLAY) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.activeAccentColor,
                cornerRadius = 28.dp
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (state.activeSheet) {
            DockSection.MODES_PLAYERS -> {
                Text(
                    text = "LUDO KING OFFLINE MODES",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GameMode.entries) { mode ->
                        val selected = state.gameMode == mode
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) state.activeAccentColor.copy(alpha = 0.28f)
                                    else Color.White.copy(alpha = 0.08f)
                                )
                                .border(
                                    width = if (selected) 1.8.dp else 1.dp,
                                    color = if (selected) state.activeAccentColor else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectGameMode(mode) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = mode.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = mode.subtitle,
                                color = Color.White.copy(alpha = 0.72f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Text(
                    text = "PLAYER COUNT (2 TO 10 PLAYERS)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items((2..10).toList()) { count ->
                        val isSelected = state.playerCount == count
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) state.activeAccentColor
                                    else Color.White.copy(alpha = 0.10f)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = Color.White.copy(alpha = if (isSelected) 0.9f else 0.25f),
                                    shape = CircleShape
                                )
                                .clickable { onPlayerCountSelect(count) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${count}P",
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            DockSection.PAWNS_DICE -> {
                Text(
                    text = "CHOOSE PAWN / TOKEN STYLE",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TokenStyle.entries) { style ->
                        val selected = state.tokenStyle == style
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) state.activeAccentColor
                                    else Color.White.copy(alpha = 0.10f)
                                )
                                .clickable { onSelectTokenStyle(style) }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = style.title,
                                color = if (selected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Text(
                    text = "CHOOSE 3D DICE SKIN",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DiceStyle.entries) { dStyle ->
                        val selected = state.diceStyle == dStyle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) state.activeAccentColor.copy(alpha = 0.30f)
                                    else Color.White.copy(alpha = 0.08f)
                                )
                                .border(
                                    width = 1.2.dp,
                                    color = if (selected) state.activeAccentColor else Color.White.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectDiceStyle(dStyle) }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = dStyle.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            DockSection.GLASS_STUDIO -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APPLE LIQUID GLASS SLIDER",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.glassModeLabel.label} (${(state.glassValue * 100).toInt()}%)",
                        color = state.activeAccentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Slider(
                    value = state.glassValue,
                    onValueChange = onGlassSliderChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = state.activeAccentColor,
                        activeTrackColor = state.activeAccentColor,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PresetPalettes) { palette ->
                        val selected = !state.useCustomColor && state.selectedPalette.id == palette.id
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(palette.primaryAccent.copy(alpha = 0.22f))
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) Color.White else palette.primaryAccent.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { onSelectPalette(palette) }
                                .padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(palette.primaryAccent)
                            )
                            Text(
                                text = palette.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Text(
                    text = "Custom Accent Spectrum (${state.customHue.toInt()}°)",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                Slider(
                    value = state.customHue,
                    onValueChange = onCustomHueChange,
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = state.activeAccentColor,
                        activeTrackColor = state.activeAccentColor
                    )
                )
            }

            DockSection.CAREER_AI -> {
                Text(
                    text = "TAP ANY SEAT TO TOGGLE HUMAN vs AI BOT",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.players) { p ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(p.color.copy(alpha = 0.25f))
                                .border(1.dp, p.color, RoundedCornerShape(14.dp))
                                .clickable { onToggleSeatBot(p.index) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "P${p.index + 1}: ${if (p.isBot) "🤖 AI" else "👤 HUMAN"}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotDifficulty.entries.forEach { diff ->
                        val selected = state.botDifficulty == diff
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected) state.activeAccentColor
                                    else Color.White.copy(alpha = 0.08f)
                                )
                                .clickable { onSelectBotDifficulty(diff) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AI: ${diff.label}",
                                color = if (selected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { onToggleSound() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.soundEnabled) "🔊 SFX ON" else "🔇 MUTE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(state.activeAccentColor.copy(alpha = 0.28f))
                            .border(1.dp, state.activeAccentColor, RoundedCornerShape(14.dp))
                            .clickable { onRestartMatch() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔄 Restart Game",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            else -> Unit
        }
    }
}
