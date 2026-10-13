package com.ludovish.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.ludovish.app.domain.AppThemeMode
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidAuroraBackground(
    primaryColor: Color,
    accentColor: Color,
    glassValue: Float,
    themeMode: AppThemeMode,
    isSnakesMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_aurora")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bgBase = when {
        isSnakesMode -> Color(0xFF4A121A)
        themeMode == AppThemeMode.LIGHT -> Color(0xFF1E4E8C)
        else -> primaryColor
    }

    val blurDp = (16f + (1f - glassValue) * 28f).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        bgBase,
                        bgBase.copy(alpha = 0.85f),
                        Color(0xFF040B1A)
                    )
                )
            )
    ) {
        // Tilted Ludo / Snakes Pattern Wallpaper (matches reference video background)
        Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(degrees = -22f) {
                val tileSize = size.width * 0.28f
                for (r in -2..8) {
                    for (c in -2..6) {
                        val topLeft = Offset(c * tileSize, r * tileSize)
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.045f),
                            topLeft = topLeft,
                            size = Size(tileSize - 12f, tileSize - 12f),
                            cornerRadius = CornerRadius(18f, 18f),
                            style = Stroke(width = 3f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.035f),
                            radius = tileSize * 0.14f,
                            center = Offset(topLeft.x + tileSize * 0.3f, topLeft.y + tileSize * 0.3f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.035f),
                            radius = tileSize * 0.14f,
                            center = Offset(topLeft.x + tileSize * 0.7f, topLeft.y + tileSize * 0.7f)
                        )
                    }
                }
            }
        }

        // Apple Liquid Glass Floating Light Orbs
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurDp)
        ) {
            val w = size.width
            val h = size.height
            val orb1 = Offset(w * (0.25f + 0.18f * cos(phase)), h * (0.22f + 0.12f * sin(phase)))
            val orb2 = Offset(w * (0.75f + 0.16f * sin(phase * 0.8f)), h * (0.72f + 0.14f * cos(phase * 0.8f)))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor.copy(alpha = 0.28f), Color.Transparent),
                    center = orb1,
                    radius = w * 0.55f
                ),
                center = orb1,
                radius = w * 0.55f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00F5D4).copy(alpha = 0.22f), Color.Transparent),
                    center = orb2,
                    radius = w * 0.60f
                ),
                center = orb2,
                radius = w * 0.60f
            )
        }

        content()
    }
}
