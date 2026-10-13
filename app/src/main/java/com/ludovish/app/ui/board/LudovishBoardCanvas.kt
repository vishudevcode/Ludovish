package com.ludovish.app.ui.board

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ludovish.app.domain.GameUiState
import com.ludovish.app.domain.Player
import com.ludovish.app.domain.Token
import com.ludovish.app.domain.TokenStyle
import com.ludovish.app.ui.theme.liquidGlassPanel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private data class RenderedTokenHitBox(
    val playerIndex: Int,
    val tokenId: Int,
    val center: Offset,
    val radius: Float
)

@Composable
fun LudovishBoardCanvas(
    state: GameUiState,
    onTokenTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "token_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(540, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val hitBoxes = remember { mutableStateListOf<RenderedTokenHitBox>() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
            .liquidGlassPanel(
                glassAmount = state.glassValue,
                accentColor = state.activeAccentColor,
                cornerRadius = 32.dp
            )
            .padding(12.dp)
            .pointerInput(state.currentTurnIndex, state.hasRolledThisTurn, state.diceValue, state.players, state.gameMode) {
                detectTapGestures { tapOffset ->
                    if (!state.hasRolledThisTurn || state.isRolling) return@detectTapGestures
                    val tapped = hitBoxes.firstOrNull { box ->
                        hypot(tapOffset.x - box.center.x, tapOffset.y - box.center.y) <= box.radius * 1.75f
                    }
                    if (tapped != null) {
                        onTokenTapped(tapped.tokenId)
                    }
                }
            }
    ) {
        hitBoxes.clear()
        if (state.playerCount <= 4) {
            drawClassicFourArmBoard(
                state = state,
                pulseScale = pulseScale,
                onRegisterHitBox = { hitBoxes.add(it) }
            )
        } else {
            drawRadialMultiPlayerArena(
                state = state,
                pulseScale = pulseScale,
                onRegisterHitBox = { hitBoxes.add(it) }
            )
        }
    }
}

private fun DrawScope.drawClassicFourArmBoard(
    state: GameUiState,
    pulseScale: Float,
    onRegisterHitBox: (RenderedTokenHitBox) -> Unit
) {
    val gridSize = 15
    val cell = size.width / gridSize

    for (r in 0 until gridSize) {
        for (c in 0 until gridSize) {
            val isTrack = (r in 6..8) || (c in 6..8)
            val isCenter = (r in 6..8) && (c in 6..8)
            if (isTrack && !isCenter) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.08f + state.glassValue * 0.10f),
                    topLeft = Offset(c * cell + 1.5f, r * cell + 1.5f),
                    size = Size(cell - 3f, cell - 3f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
    }

    val cornerColors = listOf(
        state.players.getOrNull(0)?.color ?: Color(0xFFFF3B5C),
        state.players.getOrNull(1)?.color ?: Color(0xFF00E575),
        state.players.getOrNull(2)?.color ?: Color(0xFFFFD60A),
        state.players.getOrNull(3)?.color ?: Color(0xFF0A84FF)
    )

    for (i in 1..5) {
        drawColoredCell(i, 7, cell, cornerColors[0])
        drawColoredCell(7, i, cell, cornerColors[1])
        drawColoredCell(14 - i, 7, cell, cornerColors[2])
        drawColoredCell(7, 14 - i, cell, cornerColors[3])
    }
    drawColoredCell(1, 6, cell, cornerColors[0])
    drawColoredCell(8, 1, cell, cornerColors[1])
    drawColoredCell(13, 8, cell, cornerColors[2])
    drawColoredCell(6, 13, cell, cornerColors[3])

    val starCells = listOf(
        1 to 6, 6 to 2, 8 to 1, 12 to 6,
        13 to 8, 8 to 12, 6 to 13, 2 to 8
    )
    starCells.forEach { (col, row) ->
        drawStarEmblem(
            center = Offset((col + 0.5f) * cell, (row + 0.5f) * cell),
            radius = cell * 0.28f,
            color = Color.White.copy(alpha = 0.55f)
        )
    }

    val baseOrigins = listOf(0 to 0, 9 to 0, 9 to 9, 0 to 9)
    baseOrigins.forEachIndexed { idx, (col, row) ->
        val pColor = cornerColors[idx]
        val activePlayerExists = idx < state.playerCount
        val topLeft = Offset(col * cell + 4f, row * cell + 4f)
        val boxSize = Size(cell * 6 - 8f, cell * 6 - 8f)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    pColor.copy(alpha = if (activePlayerExists) 0.36f else 0.10f),
                    pColor.copy(alpha = if (activePlayerExists) 0.14f else 0.04f)
                ),
                start = topLeft,
                end = Offset(topLeft.x + boxSize.width, topLeft.y + boxSize.height)
            ),
            topLeft = topLeft,
            size = boxSize,
            cornerRadius = CornerRadius(28f, 28f)
        )
        drawRoundRect(
            color = pColor.copy(alpha = if (idx == state.currentTurnIndex) 0.95f else 0.45f),
            topLeft = topLeft,
            size = boxSize,
            cornerRadius = CornerRadius(28f, 28f),
            style = Stroke(width = if (idx == state.currentTurnIndex) 5f else 2f)
        )
    }

    val centerTopLeft = Offset(6 * cell, 6 * cell)
    val centerSize = Size(3 * cell, 3 * cell)
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(state.activeAccentColor.copy(alpha = 0.55f), Color.White.copy(alpha = 0.12f)),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = cell * 2.2f
        ),
        topLeft = centerTopLeft,
        size = centerSize,
        cornerRadius = CornerRadius(18f, 18f)
    )

    val perimeterCoords = buildClassic52Perimeter()
    state.players.forEach { player ->
        player.tokens.forEach { token ->
            val tokenCenter = computeClassicTokenOffset(
                player = player,
                token = token,
                cell = cell,
                perimeter = perimeterCoords
            )
            val isMovable = player.index == state.currentTurnIndex &&
                state.hasRolledThisTurn &&
                !state.isRolling &&
                token.canMove(state.diceValue, state.gameMode, player.hasCapturedEnemy)

            val radius = cell * 0.38f
            drawStyledToken(
                style = state.tokenStyle,
                center = tokenCenter,
                radius = radius,
                color = player.color,
                isMovable = isMovable,
                pulseScale = pulseScale
            )
            if (isMovable) {
                onRegisterHitBox(RenderedTokenHitBox(player.index, token.id, tokenCenter, radius))
            }
        }
    }
}

private fun DrawScope.drawRadialMultiPlayerArena(
    state: GameUiState,
    pulseScale: Float,
    onRegisterHitBox: (RenderedTokenHitBox) -> Unit
) {
    val n = state.playerCount
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = size.minDimension / 2f
    val ringRadius = maxRadius * 0.64f
    val yardRadius = maxRadius * 0.87f
    val crownRadius = maxRadius * 0.17f

    val totalTrackNodes = 52
    for (i in 0 until totalTrackNodes) {
        val angle = (2.0 * PI * i / totalTrackNodes) - (PI / 2.0)
        val nodeCenter = Offset(
            x = center.x + (ringRadius * cos(angle)).toFloat(),
            y = center.y + (ringRadius * sin(angle)).toFloat()
        )
        val isSafe = i % 6 == 0
        drawCircle(
            color = if (isSafe) Color.White.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.12f),
            radius = maxRadius * 0.036f,
            center = nodeCenter
        )
    }

    state.players.forEach { player ->
        val sectorAngle = (2.0 * PI * player.index / n) - (PI / 2.0)
        val isCurrentTurn = player.index == state.currentTurnIndex

        for (step in 1..5) {
            val r = ringRadius - (step / 6f) * (ringRadius - crownRadius)
            val stepOffset = Offset(
                x = center.x + (r * cos(sectorAngle)).toFloat(),
                y = center.y + (r * sin(sectorAngle)).toFloat()
            )
            drawCircle(
                color = player.color.copy(alpha = 0.55f),
                radius = maxRadius * 0.032f,
                center = stepOffset
            )
        }

        val yardCenter = Offset(
            x = center.x + (yardRadius * cos(sectorAngle)).toFloat(),
            y = center.y + (yardRadius * sin(sectorAngle)).toFloat()
        )
        val podRadius = maxRadius * 0.115f
        drawCircle(
            color = player.color.copy(alpha = if (isCurrentTurn) 0.42f else 0.20f),
            radius = podRadius,
            center = yardCenter
        )
        drawCircle(
            color = player.color.copy(alpha = if (isCurrentTurn) 1f else 0.55f),
            radius = podRadius,
            center = yardCenter,
            style = Stroke(width = if (isCurrentTurn) 5f else 2f)
        )

        player.tokens.forEach { token ->
            val tokenOffset = when {
                token.isInBase -> {
                    val subAngle = (PI / 2.0) * token.id + (PI / 4.0)
                    Offset(
                        x = yardCenter.x + (podRadius * 0.48f * cos(subAngle)).toFloat(),
                        y = yardCenter.y + (podRadius * 0.48f * sin(subAngle)).toFloat()
                    )
                }
                token.isFinished -> {
                    val finishAngle = sectorAngle + (token.id - 1.5) * 0.14
                    Offset(
                        x = center.x + (crownRadius * 0.48f * cos(finishAngle)).toFloat(),
                        y = center.y + (crownRadius * 0.48f * sin(finishAngle)).toFloat()
                    )
                }
                token.isInHomeStretch -> {
                    val stretchStep = token.progress - 50
                    val r = ringRadius - (stretchStep / 6f) * (ringRadius - crownRadius)
                    Offset(
                        x = center.x + (r * cos(sectorAngle)).toFloat(),
                        y = center.y + (r * sin(sectorAngle)).toFloat()
                    )
                }
                else -> {
                    val startSlot = (player.index * (totalTrackNodes / n)) % totalTrackNodes
                    val globalSlot = (startSlot + token.progress) % totalTrackNodes
                    val angle = (2.0 * PI * globalSlot / totalTrackNodes) - (PI / 2.0)
                    Offset(
                        x = center.x + (ringRadius * cos(angle)).toFloat(),
                        y = center.y + (ringRadius * sin(angle)).toFloat()
                    )
                }
            }

            val isMovable = isCurrentTurn &&
                state.hasRolledThisTurn &&
                !state.isRolling &&
                token.canMove(state.diceValue, state.gameMode, player.hasCapturedEnemy)

            val tokenRadius = maxRadius * 0.042f
            drawStyledToken(
                style = state.tokenStyle,
                center = tokenOffset,
                radius = tokenRadius,
                color = player.color,
                isMovable = isMovable,
                pulseScale = pulseScale
            )
            if (isMovable) {
                onRegisterHitBox(RenderedTokenHitBox(player.index, token.id, tokenOffset, tokenRadius))
            }
        }
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(state.activeAccentColor.copy(alpha = 0.75f), Color.White.copy(alpha = 0.15f)),
            center = center,
            radius = crownRadius
        ),
        radius = crownRadius,
        center = center
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.7f),
        radius = crownRadius,
        center = center,
        style = Stroke(width = 3f)
    )
}

/**
 * Renders all 6 custom Ludo King Pawn/Token styles with specular Liquid Glass highlights
 */
private fun DrawScope.drawStyledToken(
    style: TokenStyle,
    center: Offset,
    radius: Float,
    color: Color,
    isMovable: Boolean,
    pulseScale: Float
) {
    val r = if (isMovable) radius * pulseScale else radius

    if (isMovable) {
        drawCircle(
            color = Color.White.copy(alpha = 0.65f),
            radius = r * 1.48f,
            center = center,
            style = Stroke(width = 4.5f)
        )
    }

    when (style) {
        TokenStyle.CLASSIC_PIN -> {
            // Classic Ludo King 3D Teardrop Pawn Pin
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = r * 0.9f,
                center = Offset(center.x, center.y + r * 0.25f)
            )
            val pinPath = Path().apply {
                moveTo(center.x, center.y - r * 1.25f)
                cubicTo(
                    center.x + r * 1.15f, center.y - r * 0.4f,
                    center.x + r * 0.95f, center.y + r * 0.85f,
                    center.x, center.y + r * 0.95f
                )
                cubicTo(
                    center.x - r * 0.95f, center.y + r * 0.85f,
                    center.x - r * 1.15f, center.y - r * 0.4f,
                    center.x, center.y - r * 1.25f
                )
                close()
            }
            drawPath(
                path = pinPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, color, color.copy(alpha = 0.85f)),
                    start = Offset(center.x - r, center.y - r),
                    end = Offset(center.x + r, center.y + r)
                )
            )
            drawPath(path = pinPath, color = Color.White, style = Stroke(width = 2.4f))
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = r * 0.32f,
                center = Offset(center.x, center.y - r * 0.35f)
            )
        }

        TokenStyle.LIQUID_ORB -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, color, color.copy(alpha = 0.85f)),
                    center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
                    radius = r * 1.3f
                ),
                radius = r,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = r,
                center = center,
                style = Stroke(width = 2.2f)
            )
        }

        TokenStyle.ROYAL_CROWN -> {
            drawCircle(color = color, radius = r, center = center)
            val crown = Path().apply {
                moveTo(center.x - r * 0.65f, center.y + r * 0.45f)
                lineTo(center.x - r * 0.75f, center.y - r * 0.35f)
                lineTo(center.x - r * 0.30f, center.y + r * 0.05f)
                lineTo(center.x, center.y - r * 0.55f)
                lineTo(center.x + r * 0.30f, center.y + r * 0.05f)
                lineTo(center.x + r * 0.75f, center.y - r * 0.35f)
                lineTo(center.x + r * 0.65f, center.y + r * 0.45f)
                close()
            }
            drawPath(crown, color = Color(0xFFFFD60A))
            drawCircle(color = Color.White, radius = r, center = center, style = Stroke(width = 2.4f))
        }

        TokenStyle.CYBER_HEX -> {
            val hex = Path().apply {
                for (i in 0 until 6) {
                    val a = (PI / 3.0) * i - (PI / 6.0)
                    val px = center.x + (r * 1.08f * cos(a)).toFloat()
                    val py = center.y + (r * 1.08f * sin(a)).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(hex, color = color)
            drawPath(hex, color = Color.White, style = Stroke(width = 2.5f))
        }

        TokenStyle.DIAMOND_GEM -> {
            val diamond = Path().apply {
                moveTo(center.x, center.y - r * 1.18f)
                lineTo(center.x + r * 1.08f, center.y)
                lineTo(center.x, center.y + r * 1.18f)
                lineTo(center.x - r * 1.08f, center.y)
                close()
            }
            drawPath(
                path = diamond,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, color, Color.Black.copy(alpha = 0.5f))
                )
            )
            drawPath(diamond, color = Color.White, style = Stroke(width = 2.4f))
        }

        TokenStyle.STAR_SHIELD -> {
            drawCircle(color = color.copy(alpha = 0.9f), radius = r, center = center)
            drawStarEmblem(center = center, radius = r * 0.85f, color = Color.White)
            drawCircle(color = Color.White, radius = r, center = center, style = Stroke(width = 2.2f))
        }
    }
}

private fun DrawScope.drawStarEmblem(center: Offset, radius: Float, color: Color) {
    val star = Path().apply {
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) radius else radius * 0.45f
            val a = (PI / 5.0) * i - (PI / 2.0)
            val px = center.x + (r * cos(a)).toFloat()
            val py = center.y + (r * sin(a)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(star, color = color)
}

private fun DrawScope.drawColoredCell(col: Int, row: Int, cell: Float, color: Color) {
    drawRoundRect(
        color = color.copy(alpha = 0.42f),
        topLeft = Offset(col * cell + 2f, row * cell + 2f),
        size = Size(cell - 4f, cell - 4f),
        cornerRadius = CornerRadius(7f, 7f)
    )
}

private fun buildClassic52Perimeter(): List<Pair<Int, Int>> = listOf(
    1 to 6, 2 to 6, 3 to 6, 4 to 6, 5 to 6,
    6 to 5, 6 to 4, 6 to 3, 6 to 2, 6 to 1, 6 to 0,
    7 to 0, 8 to 0,
    8 to 1, 8 to 2, 8 to 3, 8 to 4, 8 to 5,
    9 to 6, 10 to 6, 11 to 6, 12 to 6, 13 to 6, 14 to 6,
    14 to 7, 14 to 8,
    13 to 8, 12 to 8, 11 to 8, 10 to 8, 9 to 8,
    8 to 9, 8 to 10, 8 to 11, 8 to 12, 8 to 13, 8 to 14,
    7 to 14, 6 to 14,
    6 to 13, 6 to 12, 6 to 11, 6 to 10, 6 to 9,
    5 to 8, 4 to 8, 3 to 8, 2 to 8, 1 to 8, 0 to 8,
    0 to 7, 0 to 6
)

private fun computeClassicTokenOffset(
    player: Player,
    token: Token,
    cell: Float,
    perimeter: List<Pair<Int, Int>>
): Offset {
    val idx = player.index % 4
    if (token.isInBase) {
        val baseOrigins = listOf(0 to 0, 9 to 0, 9 to 9, 0 to 9)
        val (baseCol, baseRow) = baseOrigins[idx]
        val slotOffsets = listOf(1.8f to 1.8f, 4.2f to 1.8f, 1.8f to 4.2f, 4.2f to 4.2f)
        val (dc, dr) = slotOffsets[token.id % 4]
        return Offset((baseCol + dc) * cell, (baseRow + dr) * cell)
    }
    if (token.isFinished) {
        return Offset(7.5f * cell, 7.5f * cell)
    }
    if (token.isInHomeStretch) {
        val step = token.progress - 50
        val (col, row) = when (idx) {
            0 -> step to 7
            1 -> 7 to step
            2 -> (14 - step) to 7
            else -> 7 to (14 - step)
        }
        return Offset((col + 0.5f) * cell, (row + 0.5f) * cell)
    }
    val startOffsets = listOf(0, 13, 26, 39)
    val globalIdx = (startOffsets[idx] + token.progress) % 52
    val (col, row) = perimeter[globalIdx]
    return Offset((col + 0.5f) * cell, (row + 0.5f) * cell)
}
