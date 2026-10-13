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
 * @param glassAmount 0.0f = Clear Liquid, 0.5f = Frosted Translucent, 1.0f = Opaque Matte
 */
fun Modifier.liquidGlassPanel(
    glassAmount: Float,
    accentColor: Color,
    cornerRadius: Dp = 28.dp,
    elevation: Dp = 16.dp
): Modifier {
    val clamped = glassAmount.coerceIn(0f, 1f)

    // Interpolate surface alpha from Clear (0.08f) -> Frosted (0.28f) -> Opaque (0.92f)
    val baseAlpha = when {
        clamped <= 0.5f -> 0.08f + (clamped * 0.40f)
        else -> 0.28f + ((clamped - 0.5f) * 1.28f)
    }.coerceIn(0.06f, 0.94f)

    val topHighlightAlpha = (0.55f - clamped * 0.25f).coerceIn(0.18f, 0.65f)
    val accentTintAlpha = (0.12f + clamped * 0.14f).coerceIn(0.08f, 0.30f)

    val shape = RoundedCornerShape(cornerRadius)

    val surfaceBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1E2638).copy(alpha = baseAlpha),
            accentColor.copy(alpha = accentTintAlpha * 0.6f),
            Color(0xFF0D111C).copy(alpha = (baseAlpha + 0.08f).coerceAtMost(0.96f))
        ),
        start = Offset(0f, 0f),
        end = Offset(900f, 900f)
    )

    // Apple Liquid Specular Rim (bright top-left refraction, subtle bottom-right reflection)
    val rimBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = topHighlightAlpha),
            accentColor.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.06f),
            accentColor.copy(alpha = 0.28f)
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
        .border(width = 1.3.dp, brush = rimBrush, shape = shape)
}
