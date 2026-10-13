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
import com.ludovish.app.domain.*
import kotlin.math.*

private data class TokenHitBox(
    val playerIndex: Int,
    val tokenId: Int,
    val center: Offset,
    val radius: Float
)

@Composable
fun LudovishBoardCanvas(
    state: GameUiState,
    onTokenTapped: (Int, Int) -> Unit,
    onCenterDiceTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "board_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val hitBoxes = remember { mutableStateListOf<TokenHitBox>() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(6.dp)
            .pointerInput(state.players, state.currentTurnIndex, state.hasRolledThisTurn, state.gameMode) {
                detectTapGestures { tap ->
                    if (state.playerCount >= 5) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val minDim = minOf(size.width, size.height).toFloat()
                        if (hypot(tap.x - center.x, tap.y - center.y) <= minDim * 0.14f) {
                            onCenterDiceTapped()
                            return@detectTapGestures
                        }
                    }

                    val tapped = hitBoxes.firstOrNull { box ->
                        hypot(tap.x - box.center.x, tap.y - box.center.y) <= box.radius * 1.85f
                    }
                    if (tapped != null) {
                        onTokenTapped(tapped.playerIndex, tapped.tokenId)
                    }
                }
            }
    ) {
        hitBoxes.clear()
        if (state.playerCount <= 4) {
            drawAuthenticFourPlayerBoard(
                state = state,
                pulseScale = pulseScale,
                onRegisterHit = { hitBoxes.add(it) }
            )
        } else {
            drawPolygonalMultiPlayerBoard(
                state = state,
                pulseScale = pulseScale,
                onRegisterHit = { hitBoxes.add(it) }
            )
        }
    }
}

private fun DrawScope.drawAuthenticFourPlayerBoard(
    state: GameUiState,
    pulseScale: Float,
    onRegisterHit: (TokenHitBox) -> Unit
) {
    val gridSize = 15
    val cell = size.width / gridSize

    drawRoundRect(
        color = Color.White,
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(12f, 12f)
    )

    val cBlue = state.players.getOrNull(0)?.color ?: TenPlayerColors[0]
    val cRed = state.players.getOrNull(1)?.color ?: TenPlayerColors[1]
    val cGreen = state.players.getOrNull(2)?.color ?: TenPlayerColors[2]
    val cYellow = state.players.getOrNull(3)?.color ?: TenPlayerColors[3]

    for (i in 1..5) {
        drawRect(color = cBlue, topLeft = Offset(7 * cell, (14 - i) * cell), size = Size(cell, cell))
        drawRect(color = cRed, topLeft = Offset(i * cell, 7 * cell), size = Size(cell, cell))
        drawRect(color = cGreen, topLeft = Offset(7 * cell, i * cell), size = Size(cell, cell))
        drawRect(color = cYellow, topLeft = Offset((14 - i) * cell, 7 * cell), size = Size(cell, cell))
    }
    drawRect(color = cBlue, topLeft = Offset(6 * cell, 13 * cell), size = Size(cell, cell))
    drawRect(color = cRed, topLeft = Offset(1 * cell, 6 * cell), size = Size(cell, cell))
    drawRect(color = cGreen, topLeft = Offset(8 * cell, 1 * cell), size = Size(cell, cell))
    drawRect(color = cYellow, topLeft = Offset(13 * cell, 8 * cell), size = Size(cell, cell))

    for (r in 0 until gridSize) {
        for (c in 0 until gridSize) {
            val isTrack = (r in 6..8) || (c in 6..8)
            val isCenter = (r in 6..8) && (c in 6..8)
            if (isTrack && !isCenter) {
                drawRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(c * cell, r * cell),
                    size = Size(cell, cell),
                    style = Stroke(width = 1.4f)
                )
            }
        }
    }

    val starSquares = listOf(
        6 to 13, 2 to 8, 1 to 6, 6 to 2,
        8 to 1, 12 to 6, 13 to 8, 8 to 12
    )
    starSquares.forEach { (col, row) ->
        drawStarOutline(
            center = Offset((col + 0.5f) * cell, (row + 0.5f) * cell),
            radius = cell * 0.34f,
            color = Color(0xFF334155)
        )
    }

    val corners = listOf(
        Triple(0, 9, cBlue),
        Triple(0, 0, cRed),
        Triple(9, 0, cGreen),
        Triple(9, 9, cYellow)
    )
    corners.forEachIndexed { idx, (col, row, color) ->
        val topLeft = Offset(col * cell, row * cell)
        val yardSize = Size(6 * cell, 6 * cell)
        drawRect(color = color, topLeft = topLeft, size = yardSize)
        drawRect(color = Color.Black, topLeft = topLeft, size = yardSize, style = Stroke(width = 2f))

        val innerPad = cell * 0.85f
        val innerTopLeft = Offset(topLeft.x + innerPad, topLeft.y + innerPad)
        val innerSize = Size(6 * cell - innerPad * 2f, 6 * cell - innerPad * 2f)
        drawRoundRect(
            color = Color.White,
            topLeft = innerTopLeft,
            size = innerSize,
            cornerRadius = CornerRadius(18f, 18f)
        )

        val circleOffsets = listOf(1.9f to 1.9f, 4.1f to 1.9f, 1.9f to 4.1f, 4.1f to 4.1f)
        circleOffsets.forEach { (dc, dr) ->
            drawCircle(
                color = color,
                radius = cell * 0.56f,
                center = Offset((col + dc) * cell, (row + dr) * cell)
            )
        }

        if (state.gameMode == GameMode.TEAM_UP) {
            val badgeCenter = when (idx) {
                0 -> Offset(5.4f * cell, 9.6f * cell)
                1 -> Offset(5.4f * cell, 5.4f * cell)
                2 -> Offset(9.6f * cell, 5.4f * cell)
                else -> Offset(9.6f * cell, 9.6f * cell)
            }
            drawCircle(
                color = if (idx % 2 == 0) Color(0xFF0088FF) else Color(0xFFE6192E),
                radius = cell * 0.42f,
                center = badgeCenter
            )
            drawCircle(
                color = Color.White,
                radius = cell * 0.42f,
                center = badgeCenter,
                style = Stroke(width = 2.5f)
            )
        }
    }

    val cx = 7.5f * cell
    val cy = 7.5f * cell
    val cLeft = 6f * cell
    val cRight = 9f * cell
    val cTop = 6f * cell
    val cBottom = 9f * cell

    val triBottom = Path().apply { moveTo(cLeft, cBottom); lineTo(cRight, cBottom); lineTo(cx, cy); close() }
    val triLeft = Path().apply { moveTo(cLeft, cTop); lineTo(cLeft, cBottom); lineTo(cx, cy); close() }
    val triTop = Path().apply { moveTo(cLeft, cTop); lineTo(cRight, cTop); lineTo(cx, cy); close() }
    val triRight = Path().apply { moveTo(cRight, cTop); lineTo(cRight, cBottom); lineTo(cx, cy); close() }

    drawPath(triBottom, color = cBlue)
    drawPath(triLeft, color = cRed)
    drawPath(triTop, color = cGreen)
    drawPath(triRight, color = cYellow)
    drawRect(color = Color.Black, topLeft = Offset(cLeft, cTop), size = Size(3 * cell, 3 * cell), style = Stroke(width = 2f))

    val perimeter = buildAuthentic52Track()
    state.players.forEach { player ->
        player.tokens.forEach { token ->
            val pos = computeFourPlayerTokenOffset(player.index, token, cell, perimeter)
            val canMove = (state.gameMode == GameMode.RUSH_MODE || (player.index == state.currentTurnIndex && state.hasRolledThisTurn && !state.isRolling)) &&
                token.canMove(if (state.gameMode == GameMode.RUSH_MODE) player.diceValue else state.diceValue)

            val r = cell * 0.40f
            drawLudoKingPawn(
                style = state.tokenStyle,
                center = pos,
                radius = r,
                color = player.color,
                isMovable = canMove,
                pulseScale = pulseScale
            )
            if (canMove) {
                onRegisterHit(TokenHitBox(player.index, token.id, pos, r))
            }
        }
    }
}

private fun DrawScope.drawPolygonalMultiPlayerBoard(
    state: GameUiState,
    pulseScale: Float,
    onRegisterHit: (TokenHitBox) -> Unit
) {
    val n = state.playerCount.coerceIn(5, 10)
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerR = size.minDimension * 0.485f
    val innerR = size.minDimension * 0.135f
    val sectorAngle = (2.0 * PI / n)
    val startBaseAngle = (PI / 2.0) - (sectorAngle / 2.0)

    for (i in 0 until n) {
        val player = state.players.getOrNull(i)
        val pColor = player?.color ?: TenPlayerColors[i % TenPlayerColors.size]
        val a1 = startBaseAngle + i * sectorAngle
        val a2 = a1 + sectorAngle
        val aMid = (a1 + a2) / 2.0

        val v1 = Offset(center.x + (outerR * cos(a1)).toFloat(), center.y + (outerR * sin(a1)).toFloat())
        val v2 = Offset(center.x + (outerR * cos(a2)).toFloat(), center.y + (outerR * sin(a2)).toFloat())
        val c1 = Offset(center.x + (innerR * cos(a1)).toFloat(), center.y + (innerR * sin(a1)).toFloat())
        val c2 = Offset(center.x + (innerR * cos(a2)).toFloat(), center.y + (innerR * sin(a2)).toFloat())

        val edgeMid = Offset((v1.x + v2.x) / 2f, (v1.y + v2.y) / 2f)
        val innerMid = Offset((c1.x + c2.x) / 2f, (c1.y + c2.y) / 2f)

        val yardPath = Path().apply {
            moveTo(v1.x, v1.y)
            lineTo(edgeMid.x, edgeMid.y)
            lineTo(innerMid.x, innerMid.y)
            lineTo(c1.x, c1.y)
            close()
        }
        drawPath(yardPath, color = pColor)
        drawPath(yardPath, color = Color.Black, style = Stroke(width = 2.5f))

        val yardCenter = Offset(
            (v1.x + edgeMid.x + innerMid.x + c1.x) / 4f,
            (v1.y + edgeMid.y + innerMid.y + c1.y) / 4f
        )
        drawCircle(color = Color.White, radius = outerR * 0.14f, center = yardCenter)
        drawCircle(color = Color.Black, radius = outerR * 0.14f, center = yardCenter, style = Stroke(width = 1.8f))

        val trackPath = Path().apply {
            moveTo(edgeMid.x, edgeMid.y)
            lineTo(v2.x, v2.y)
            lineTo(c2.x, c2.y)
            lineTo(innerMid.x, innerMid.y)
            close()
        }
        drawPath(trackPath, color = Color.White)
        drawPath(trackPath, color = Color.Black, style = Stroke(width = 2f))

        for (step in 1..5) {
            val t = step / 6f
            val rStep = outerR * (1f - t) + innerR * t
            val stretchAngle = aMid + sectorAngle * 0.22
            val pos = Offset(
                center.x + (rStep * cos(stretchAngle)).toFloat(),
                center.y + (rStep * sin(stretchAngle)).toFloat()
            )
            drawCircle(color = pColor, radius = outerR * 0.042f, center = pos)
        }

        player?.tokens?.forEach { token ->
            val tokenPos = when {
                token.isInBase -> {
                    val subA = (PI / 2.0) * token.id
                    Offset(
                        yardCenter.x + (outerR * 0.068f * cos(subA)).toFloat(),
                        yardCenter.y + (outerR * 0.068f * sin(subA)).toFloat()
                    )
                }
                token.isFinished -> {
                    Offset(
                        center.x + (innerR * 0.5f * cos(aMid)).toFloat(),
                        center.y + (innerR * 0.5f * sin(aMid)).toFloat()
                    )
                }
                token.isInHomeStretch -> {
                    val st = (token.progress - 50) / 6f
                    val rStep = outerR * (1f - st) + innerR * st
                    val stretchAngle = aMid + sectorAngle * 0.22
                    Offset(
                        center.x + (rStep * cos(stretchAngle)).toFloat(),
                        center.y + (rStep * sin(stretchAngle)).toFloat()
                    )
                }
                else -> {
                    val totalRing = 52
                    val startSlot = (player.index * (totalRing / n)) % totalRing
                    val globalSlot = (startSlot + token.progress) % totalRing
                    val ringAngle = startBaseAngle + (2.0 * PI * globalSlot / totalRing)
                    val trackR = outerR * 0.62f
                    Offset(
                        center.x + (trackR * cos(ringAngle)).toFloat(),
                        center.y + (trackR * sin(ringAngle)).toFloat()
                    )
                }
            }

            val canMove = player.index == state.currentTurnIndex &&
                state.hasRolledThisTurn &&
                !state.isRolling &&
                token.canMove(state.diceValue)

            val tokR = outerR * 0.048f
            drawLudoKingPawn(
                style = state.tokenStyle,
                center = tokenPos,
                radius = tokR,
                color = pColor,
                isMovable = canMove,
                pulseScale = pulseScale
            )
            if (canMove) {
                onRegisterHit(TokenHitBox(player.index, token.id, tokenPos, tokR))
            }
        }
    }

    val centerPoly = Path().apply {
        for (i in 0 until n) {
            val a = startBaseAngle + i * sectorAngle
            val px = center.x + (innerR * cos(a)).toFloat()
            val py = center.y + (innerR * sin(a)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    val activeColor = state.players.getOrNull(state.currentTurnIndex)?.color ?: Color.White
    drawPath(centerPoly, color = Color(0xFF0A192F))
    drawPath(centerPoly, color = Color.White, style = Stroke(width = 3f))

    val diceBoxSize = innerR * 1.15f
    drawRoundRect(
        color = activeColor,
        topLeft = Offset(center.x - diceBoxSize / 2f, center.y - diceBoxSize / 2f),
        size = Size(diceBoxSize, diceBoxSize),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(center.x - diceBoxSize / 2f, center.y - diceBoxSize / 2f),
        size = Size(diceBoxSize, diceBoxSize),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = 2.5f)
    )

    val pipR = diceBoxSize * 0.09f
    drawCircle(color = Color.White, radius = pipR, center = center)
    if (state.diceValue >= 2) {
        drawCircle(color = Color.White, radius = pipR, center = Offset(center.x - diceBoxSize * 0.25f, center.y - diceBoxSize * 0.25f))
        drawCircle(color = Color.White, radius = pipR, center = Offset(center.x + diceBoxSize * 0.25f, center.y + diceBoxSize * 0.25f))
    }
    if (state.diceValue >= 4) {
        drawCircle(color = Color.White, radius = pipR, center = Offset(center.x + diceBoxSize * 0.25f, center.y - diceBoxSize * 0.25f))
        drawCircle(color = Color.White, radius = pipR, center = Offset(center.x - diceBoxSize * 0.25f, center.y + diceBoxSize * 0.25f))
    }
}

private fun DrawScope.drawLudoKingPawn(
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
            color = Color(0xFFFFD60A),
            radius = r * 1.38f,
            center = center,
            style = Stroke(width = 4f)
        )
    }

    when (style) {
        TokenStyle.LOCATION_PIN -> {
            val pin = Path().apply {
                moveTo(center.x, center.y + r * 0.95f)
                cubicTo(
                    center.x - r * 1.15f, center.y - r * 0.1f,
                    center.x - r * 0.95f, center.y - r * 1.25f,
                    center.x, center.y - r * 1.25f
                )
                cubicTo(
                    center.x + r * 0.95f, center.y - r * 1.25f,
                    center.x + r * 1.15f, center.y - r * 0.1f,
                    center.x, center.y + r * 0.95f
                )
                close()
            }
            drawPath(pin, color = color)
            drawPath(pin, color = Color.White, style = Stroke(width = 2.6f))
            drawCircle(color = Color.White, radius = r * 0.36f, center = Offset(center.x, center.y - r * 0.42f))
        }
        else -> {
            drawCircle(color = color, radius = r, center = center)
            drawCircle(color = Color.White, radius = r, center = center, style = Stroke(width = 2.6f))
            drawStarOutline(center = center, radius = r * 0.55f, color = Color.White)
        }
    }
}

private fun DrawScope.drawStarOutline(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        for (i in 0 until 10) {
            val rad = if (i % 2 == 0) radius else radius * 0.42f
            val a = (PI / 5.0) * i - (PI / 2.0)
            val px = center.x + (rad * cos(a)).toFloat()
            val py = center.y + (rad * sin(a)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(path, color = color, style = Stroke(width = 2f))
}

private fun buildAuthentic52Track(): List<Pair<Int, Int>> = listOf(
    6 to 13, 6 to 12, 6 to 11, 6 to 10, 6 to 9,
    5 to 8, 4 to 8, 3 to 8, 2 to 8, 1 to 8, 0 to 8,
    0 to 7, 0 to 6,
    1 to 6, 2 to 6, 3 to 6, 4 to 6, 5 to 6,
    6 to 5, 6 to 4, 6 to 3, 6 to 2, 6 to 1, 6 to 0,
    7 to 0, 8 to 0,
    8 to 1, 8 to 2, 8 to 3, 8 to 4, 8 to 5,
    9 to 6, 10 to 6, 11 to 6, 12 to 6, 13 to 6, 14 to 6,
    14 to 7, 14 to 8,
    13 to 8, 12 to 8, 11 to 8, 10 to 8, 9 to 8,
    8 to 9, 8 to 10, 8 to 11, 8 to 12, 8 to 13, 8 to 14,
    7 to 14, 6 to 14
)

private fun computeFourPlayerTokenOffset(
    playerIndex: Int,
    token: Token,
    cell: Float,
    perimeter: List<Pair<Int, Int>>
): Offset {
    val idx = playerIndex % 4
    if (token.isInBase) {
        val corners = listOf(0 to 9, 0 to 0, 9 to 0, 9 to 9)
        val (bc, br) = corners[idx]
        val slots = listOf(1.9f to 1.9f, 4.1f to 1.9f, 1.9f to 4.1f, 4.1f to 4.1f)
        val (dc, dr) = slots[token.id % 4]
        return Offset((bc + dc) * cell, (br + dr) * cell)
    }
    if (token.isFinished) {
        return Offset(7.5f * cell, 7.5f * cell)
    }
    if (token.isInHomeStretch) {
        val step = token.progress - 50
        val (col, row) = when (idx) {
            0 -> 7 to (14 - step)
            1 -> step to 7
            2 -> 7 to step
            else -> (14 - step) to 7
        }
        return Offset((col + 0.5f) * cell, (row + 0.5f) * cell)
    }
    val startOffsets = listOf(0, 13, 26, 39)
    val global = (startOffsets[idx] + token.progress) % 52
    val (c, r) = perimeter[global]
    return Offset((c + 0.5f) * cell, (r + 0.5f) * cell)
}

@Composable
fun SnakesAndLaddersCanvas(
    theme: SnakesTheme,
    boardLayout: SnakesBoardLayout,
    players: List<Player>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        val cell = size.width / 10f

        for (row in 0 until 10) {
            for (col in 0 until 10) {
                val isEven = (row + col) % 2 == 0
                drawRect(
                    color = if (isEven) theme.boardBg else theme.altTile,
                    topLeft = Offset(col * cell, row * cell),
                    size = Size(cell, cell)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.25f),
                    topLeft = Offset(col * cell, row * cell),
                    size = Size(cell, cell),
                    style = Stroke(width = 1.2f)
                )
            }
        }

        boardLayout.ladders.forEach { (startSq, endSq) ->
            val p1 = squareToOffset(startSq, cell)
            val p2 = squareToOffset(endSq, cell)
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            val len = hypot(dx, dy).coerceAtLeast(1f)
            val nx = (-dy / len) * (cell * 0.16f)
            val ny = (dx / len) * (cell * 0.16f)

            drawLine(color = theme.ladderColor, start = Offset(p1.x + nx, p1.y + ny), end = Offset(p2.x + nx, p2.y + ny), strokeWidth = 5f)
            drawLine(color = theme.ladderColor, start = Offset(p1.x - nx, p1.y - ny), end = Offset(p2.x - nx, p2.y - ny), strokeWidth = 5f)

            val steps = (len / (cell * 0.35f)).toInt().coerceAtLeast(3)
            for (s in 1 until steps) {
                val t = s.toFloat() / steps
                val rx = p1.x + dx * t
                val ry = p1.y + dy * t
                drawLine(
                    color = theme.ladderColor,
                    start = Offset(rx + nx, ry + ny),
                    end = Offset(rx - nx, ry - ny),
                    strokeWidth = 3.5f
                )
            }
        }

        boardLayout.snakes.forEach { (headSq, tailSq) ->
            val head = squareToOffset(headSq, cell)
            val tail = squareToOffset(tailSq, cell)
            val mid = Offset((head.x + tail.x) / 2f, (head.y + tail.y) / 2f)
            val snakePath = Path().apply {
                moveTo(head.x, head.y)
                quadraticTo(mid.x + cell * 0.9f, (head.y + mid.y) / 2f, mid.x, mid.y)
                quadraticTo(mid.x - cell * 0.9f, (mid.y + tail.y) / 2f, tail.x, tail.y)
            }
            drawPath(snakePath, color = theme.snakeColor, style = Stroke(width = 9f, cap = StrokeCap.Round))
            drawPath(snakePath, color = Color.Yellow.copy(alpha = 0.7f), style = Stroke(width = 3f, cap = StrokeCap.Round))
            drawCircle(color = theme.snakeColor, radius = cell * 0.22f, center = head)
        }

        players.forEachIndexed { idx, p ->
            val pos = squareToOffset(p.snakesPosition, cell)
            val shift = if (idx == 0) -cell * 0.14f else cell * 0.14f
            drawCircle(
                color = p.color,
                radius = cell * 0.28f,
                center = Offset(pos.x + shift, pos.y)
            )
            drawCircle(
                color = Color.White,
                radius = cell * 0.28f,
                center = Offset(pos.x + shift, pos.y),
                style = Stroke(width = 2.5f)
            )
        }
    }
}

private fun squareToOffset(square: Int, cell: Float): Offset {
    val clamped = square.coerceIn(1, 100) - 1
    val rowFromBottom = clamped / 10
    val colInRow = clamped % 10
    val col = if (rowFromBottom % 2 == 0) colInRow else (9 - colInRow)
    val row = 9 - rowFromBottom
    return Offset((col + 0.5f) * cell, (row + 0.5f) * cell)
}
