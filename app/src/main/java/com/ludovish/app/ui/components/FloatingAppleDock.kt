package com.ludovish.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludovish.app.domain.AvatarEmojis
import com.ludovish.app.domain.BottomNavTab
import com.ludovish.app.domain.CareerStats
import com.ludovish.app.ui.theme.liquidGlassPanel

@Composable
fun LudoKingTopHeader(
    stats: CareerStats,
    glassValue: Float,
    accentColor: Color,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .liquidGlassPanel(
                glassAmount = glassValue,
                accentColor = accentColor,
                cornerRadius = 22.dp,
                elevation = 10.dp
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Avatar + Level Star + Name ("TrollDrako")
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable { onProfileClick() }
                .padding(end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(contentAlignment = Alignment.BottomStart) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E3A8A))
                        .border(2.dp, Color(0xFFFFD60A), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AvatarEmojis[stats.avatarIndex % AvatarEmojis.size],
                        fontSize = 24.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .offset(x = (-4).dp, y = 4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFBE0B))
                        .border(1.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${stats.level}",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column {
                Text(
                    text = stats.playerName,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Tap for Stats",
                    color = Color(0xFFFFD60A),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Center: Gems (150) & Coins (2,650) Counter Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurrencyPill(icon = "💎", amount = "%,d".format(stats.gems))
            CurrencyPill(icon = "🪙", amount = "%,d".format(stats.coins))
        }

        // Right: Settings Gear Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1D4ED8))
                .border(1.5.dp, Color(0xFFFFD60A), RoundedCornerShape(12.dp))
                .clickable { onSettingsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = Color(0xFFFFD60A),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CurrencyPill(icon: String, amount: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF07132B).copy(alpha = 0.85f))
            .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 12.sp)
        Text(
            text = amount,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun FloatingAppleDock(
    selectedTab: BottomNavTab,
    glassValue: Float,
    accentColor: Color,
    onSelectTab: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val icons = mapOf(
        BottomNavTab.HOME to Icons.Rounded.Home,
        BottomNavTab.EVENT to Icons.Rounded.Palette,
        BottomNavTab.ADDA to Icons.Rounded.FolderOpen,
        BottomNavTab.INVENTORY to Icons.Rounded.Category,
        BottomNavTab.SOCIAL to Icons.Rounded.BarChart
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .liquidGlassPanel(
                glassAmount = glassValue,
                accentColor = accentColor,
                cornerRadius = 30.dp,
                elevation = 20.dp
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                label = "dock_tab"
            )
            Column(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationY = if (isSelected) -5f else 0f
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent)
                    .clickable { onSelectTab(tab) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icons[tab] ?: Icons.Rounded.Home,
                    contentDescription = tab.label,
                    tint = if (isSelected) accentColor else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = tab.label,
                    color = if (isSelected) accentColor else Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
