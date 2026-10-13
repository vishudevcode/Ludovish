package com.ludovish.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ludovish.app.domain.DiceStyle
import com.ludovish.app.ui.theme.liquidGlassPanel

@Composable
fun ThreeDDiceView(
    diceValue: Int,
    diceStyle: DiceStyle,
    isRolling: Boolean,
    playerColor: Color,
    glassValue: Float,
    onRollClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 360f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "dice_spin"
    )
    val scale by animateFloatAsState(
        targetValue = if (isRolling) 1.18f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dice_scale"
    )

    val skinAccent = when (diceStyle) {
        DiceStyle.LIQUID_CRYSTAL -> playerColor
        DiceStyle.IMPERIAL_GOLD -> Color(0xFFFFD60A)
        DiceStyle.NEON_MATRIX -> Color(0xFF00F5D4)
        DiceStyle.OBSIDIAN_DARK -> Color(0xFF94A3B8)
    }

    Canvas(
        modifier = Modifier
            .size(72.dp)
            .graphicsLayer {
                rotationZ = rotation
                rotationX = rotation * 0.5f
                scaleX = scale
                scaleY = scale
            }
            .liquidGlassPanel(
                glassAmount = glassValue,
                accentColor = skinAccent,
                cornerRadius = 22.dp,
                elevation = 20.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onRollClick
            )
    ) {
        val w = size.width
        val h = size.height

        if (diceStyle == DiceStyle.IMPERIAL_GOLD) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD60A).copy(alpha = 0.45f), Color(0xFFFB5607).copy(alpha = 0.25f))
                ),
                cornerRadius = CornerRadius(22.dp.toPx())
            )
        } else if (diceStyle == DiceStyle.OBSIDIAN_DARK) {
            drawRoundRect(
                color = Color(0xFF090D16).copy(alpha = 0.82f),
                cornerRadius = CornerRadius(22.dp.toPx())
            )
        }

        val pipRadius = w * 0.088f
        val positions = mapOf(
            "TL" to Offset(w * 0.27f, h * 0.27f),
            "TR" to Offset(w * 0.73f, h * 0.27f),
            "ML" to Offset(w * 0.27f, h * 0.50f),
            "MC" to Offset(w * 0.50f, h * 0.50f),
            "MR" to Offset(w * 0.73f, h * 0.50f),
            "BL" to Offset(w * 0.27f, h * 0.73f),
            "BR" to Offset(w * 0.73f, h * 0.73f)
        )

        val activePips = when (diceValue) {
            1 -> listOf("MC")
            2 -> listOf("TL", "BR")
            3 -> listOf("TL", "MC", "BR")
            4 -> listOf("TL", "TR", "BL", "BR")
            5 -> listOf("TL", "TR", "MC", "BL", "BR")
            else -> listOf("TL", "TR", "ML", "MR", "BL", "BR")
        }

        activePips.forEach { key ->
            positions[key]?.let { pos ->
                drawCircle(
                    color = if (diceValue == 6) skinAccent else Color.White,
                    radius = pipRadius,
                    center = pos
                )
                if (diceStyle == DiceStyle.NEON_MATRIX) {
                    drawCircle(
                        color = skinAccent,
                        radius = pipRadius * 1.35f,
                        center = pos,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}
