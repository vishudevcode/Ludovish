package com.ludovish.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludovish.app.domain.DockSection
import com.ludovish.app.ui.theme.liquidGlassPanel

@Composable
fun FloatingAppleDock(
    selectedSection: DockSection,
    glassValue: Float,
    accentColor: Color,
    onSelectSection: (DockSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .liquidGlassPanel(
                glassAmount = glassValue,
                accentColor = accentColor,
                cornerRadius = 38.dp,
                elevation = 24.dp
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DockItem(
            icon = Icons.Rounded.SportsEsports,
            label = "Board",
            isSelected = selectedSection == DockSection.PLAY,
            accentColor = accentColor,
            onClick = { onSelectSection(DockSection.PLAY) }
        )
        DockItem(
            icon = Icons.Rounded.Groups,
            label = "Modes",
            isSelected = selectedSection == DockSection.MODES_PLAYERS,
            accentColor = accentColor,
            onClick = { onSelectSection(DockSection.MODES_PLAYERS) }
        )
        DockItem(
            icon = Icons.Rounded.Category,
            label = "Pawns",
            isSelected = selectedSection == DockSection.PAWNS_DICE,
            accentColor = accentColor,
            onClick = { onSelectSection(DockSection.PAWNS_DICE) }
        )
        DockItem(
            icon = Icons.Rounded.Palette,
            label = "Glass UI",
            isSelected = selectedSection == DockSection.GLASS_STUDIO,
            accentColor = accentColor,
            onClick = { onSelectSection(DockSection.GLASS_STUDIO) }
        )
        DockItem(
            icon = Icons.Rounded.AutoAwesome,
            label = "AI & Audio",
            isSelected = selectedSection == DockSection.CAREER_AI,
            accentColor = accentColor,
            onClick = { onSelectSection(DockSection.CAREER_AI) }
        )
    }
}

@Composable
private fun DockItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.18f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dock_magnify"
    )

    Column(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (isSelected) -7f else 0f
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) accentColor.copy(alpha = 0.28f)
                    else Color.White.copy(alpha = 0.07f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentColor else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (isSelected) accentColor else Color.White.copy(alpha = 0.72f),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
