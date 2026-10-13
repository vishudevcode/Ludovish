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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ludovish.app.domain.ThemePalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidAuroraBackground(
    palette: ThemePalette,
    activeAccent: Color,
    glassValue: Float,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_aurora")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Dynamic frost blur on background orbs controlled by user's Glass Slider
    val dynamicBlur = (24f + (1f - glassValue) * 42f).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.deepBackground)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(dynamicBlur)
        ) {
            val w = size.width
            val h = size.height

            val orb1Center = Offset(
                x = w * (0.28f + 0.18f * cos(phase)),
                y = h * (0.24f + 0.12f * sin(phase))
            )
            val orb2Center = Offset(
                x = w * (0.72f + 0.18f * sin(phase * 0.8f)),
                y = h * (0.68f + 0.15f * cos(phase * 0.8f))
            )
            val orb3Center = Offset(
                x = w * (0.5f + 0.22f * sin(phase * 1.2f)),
                y = h * (0.45f + 0.18f * cos(phase * 0.6f))
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(activeAccent.copy(alpha = 0.42f), Color.Transparent),
                    center = orb1Center,
                    radius = w * 0.62f
                ),
                center = orb1Center,
                radius = w * 0.62f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.secondaryOrb.copy(alpha = 0.38f), Color.Transparent),
                    center = orb2Center,
                    radius = w * 0.68f
                ),
                center = orb2Center,
                radius = w * 0.68f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                    center = orb3Center,
                    radius = w * 0.45f
                ),
                center = orb3Center,
                radius = w * 0.45f
            )
        }

        content()
    }
}
