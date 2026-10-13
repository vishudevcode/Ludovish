package com.ludovish.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LudoCornerDiceTray(
    playerName: String,
    playerColor: Color,
    diceValue: Int,
    isCurrentTurn: Boolean,
    isRolling: Boolean,
    alignRight: Boolean,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arrow_bounce")
    val arrowShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (alignRight && isCurrentTurn) {
            Text(
                text = "▶",
                color = Color(0xFFFFCC00),
                fontSize = 20.sp,
                modifier = Modifier.graphicsLayer { translationX = -arrowShift }
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A3A6E).copy(alpha = 0.88f))
                .border(
                    width = if (isCurrentTurn) 2.2.dp else 1.2.dp,
                    color = if (isCurrentTurn) Color(0xFFFFD60A) else Color.White.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onRollClick() }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Player Color Token Emblem
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(playerColor)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⭐", fontSize = 13.sp)
            }

            // 3D Dice Box (Active on Turn or Rush Mode)
            ThreeDDiceBox(
                diceValue = diceValue,
                isRolling = isRolling,
                showDice = isCurrentTurn,
                onClick = onRollClick
            )
        }

        if (!alignRight && isCurrentTurn) {
            Text(
                text = "◀",
                color = Color(0xFFFFCC00),
                fontSize = 20.sp,
                modifier = Modifier.graphicsLayer { translationX = arrowShift }
            )
        }
    }
}

@Composable
fun ThreeDDiceBox(
    diceValue: Int,
    isRolling: Boolean,
    showDice: Boolean = true,
    onClick: () -> Unit
) {
    val spin by animateFloatAsState(
        targetValue = if (isRolling) 360f else 0f,
        animationSpec = tween(260),
        label = "spin"
    )

    Canvas(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer {
                rotationZ = spin
                scaleX = if (isRolling) 1.18f else 1f
                scaleY = if (isRolling) 1.18f else 1f
            }
            .clickable { onClick() }
    ) {
        drawRoundRect(
            color = if (showDice) Color(0xFFF8FAFC) else Color(0xFFCBD5E1).copy(alpha = 0.45f),
            cornerRadius = CornerRadius(10.dp.toPx())
        )
        if (!showDice) return@Canvas

        val w = size.width
        val h = size.height
        val r = w * 0.095f
        val pips = mapOf(
            "TL" to Offset(w * 0.26f, h * 0.26f),
            "TR" to Offset(w * 0.74f, h * 0.26f),
            "ML" to Offset(w * 0.26f, h * 0.50f),
            "MC" to Offset(w * 0.50f, h * 0.50f),
            "MR" to Offset(w * 0.74f, h * 0.50f),
            "BL" to Offset(w * 0.26f, h * 0.74f),
            "BR" to Offset(w * 0.74f, h * 0.74f)
        )
        val keys = when (diceValue) {
            1 -> listOf("MC")
            2 -> listOf("TL", "BR")
            3 -> listOf("TL", "MC", "BR")
            4 -> listOf("TL", "TR", "BL", "BR")
            5 -> listOf("TL", "TR", "MC", "BL", "BR")
            else -> listOf("TL", "TR", "ML", "MR", "BL", "BR")
        }
        keys.forEach { k ->
            pips[k]?.let { pt ->
                drawCircle(
                    color = if (diceValue == 6) Color(0xFFE6192E) else Color(0xFF0F172A),
                    radius = r,
                    center = pt
                )
            }
        }
    }
}
