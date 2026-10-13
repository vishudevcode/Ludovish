package com.ludovish.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple Liquid Glass / Frosted Glassmorphic Modifier
 * @param glassAmount 0.0f = Clear Liquid, 0.5f = Frosted Translucent, 1.0f = Opaque Matte
 */
fun Modifier.liquidGlassPanel(
    glassAmount: Float,
    accentColor: Color,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 14.dp,
    baseSurfaceColor: Color = Color(0xFF0E2248)
): Modifier {
    val clamped = glassAmount.coerceIn(0f, 1f)

    val baseAlpha = when {
        clamped <= 0.5f -> 0.16f + (clamped * 0.48f)
        else -> 0.40f + ((clamped - 0.5f) * 1.15f)
    }.coerceIn(0.12f, 0.97f)

    val topHighlightAlpha = (0.68f - clamped * 0.25f).coerceIn(0.25f, 0.75f)
    val shape = RoundedCornerShape(cornerRadius)

    val surfaceBrush = Brush.linearGradient(
        colors = listOf(
            baseSurfaceColor.copy(alpha = baseAlpha),
            accentColor.copy(alpha = (0.12f + clamped * 0.10f).coerceAtMost(0.32f)),
            Color(0xFF061024).copy(alpha = (baseAlpha + 0.08f).coerceAtMost(0.98f))
        ),
        start = Offset(0f, 0f),
        end = Offset(900f, 900f)
    )

    val rimBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = topHighlightAlpha),
            accentColor.copy(alpha = 0.65f),
            Color.White.copy(alpha = 0.12f),
            accentColor.copy(alpha = 0.45f)
        ),
        start = Offset(0f, 0f),
        end = Offset(600f, 600f)
    )

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = accentColor.copy(alpha = 0.35f),
            spotColor = accentColor.copy(alpha = 0.45f)
        )
        .clip(shape)
        .background(surfaceBrush)
        .border(width = 1.6.dp, brush = rimBrush, shape = shape)
}
