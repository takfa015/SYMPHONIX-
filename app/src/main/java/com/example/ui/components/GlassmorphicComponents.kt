package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBorderBottom
import com.example.ui.theme.GlassBorderTop
import com.example.ui.theme.GlassCoralRed
import com.example.ui.theme.GlassEmeraldGreen
import com.example.ui.theme.GlassPureWhite
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassWaterBlue
import com.example.ui.theme.GlassWhiteCard

enum class CashTab(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    DASHBOARD("Accueil", Icons.Default.Dashboard, GlassWaterBlue),
    ENCAISSEMENT("Encaissement", Icons.Default.AddCircle, GlassEmeraldGreen),
    DECAISSEMENT("Décaissement", Icons.Default.RemoveCircle, GlassCoralRed),
    HISTORIQUE("Historique", Icons.Default.History, GlassWaterBlue),
    CLOTURE("Clôture", Icons.Default.Lock, GlassEmeraldGreen),
    PARAMETRES("Paramètres", Icons.Default.Settings, Color(0xFF64748B))
}

/**
 * Clean luminous canvas with organic fluid water curves and refractive highlights,
 * making semi-transparent glass cards look like water droplets sitting on water.
 */
@Composable
fun WaterDropletBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F8FB), // Very light airy aqua-white
                        Color(0xFFEBF5FA),
                        Color(0xFFF0FDF8), // Subtle hint of emerald freshness
                        Color(0xFFF8FAFC)
                    )
                )
            )
    ) {
        // Decorative fluid water shapes in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Organic Water Drop 1 (Top right - Cyan Blue)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3838BDF8),
                        Color(0x1838BDF8),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.55f
                ),
                center = Offset(width * 0.85f, height * 0.12f),
                radius = width * 0.55f
            )

            // Organic Water Drop 2 (Middle left - Emerald Green)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3034D399),
                        Color(0x1234D399),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.1f, height * 0.42f),
                    radius = width * 0.6f
                ),
                center = Offset(width * 0.1f, height * 0.42f),
                radius = width * 0.6f
            )

            // Organic Water Drop 3 (Bottom right - Soft Rose / Coral)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x22F43F5E),
                        Color(0x0AF43F5E),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.9f, height * 0.75f),
                    radius = width * 0.5f
                ),
                center = Offset(width * 0.9f, height * 0.75f),
                radius = width * 0.5f
            )

            // Fluid water curve
            val wavePath = Path().apply {
                moveTo(0f, height * 0.28f)
                cubicTo(
                    width * 0.35f, height * 0.22f,
                    width * 0.65f, height * 0.34f,
                    width, height * 0.26f
                )
                lineTo(width, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x1A0284C7),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height * 0.3f
                )
            )
        }

        content()
    }
}

/**
 * Ultra-clear water droplet glass card with specular highlights and curved refraction borders.
 */
@Composable
fun WaterDropCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    borderWidth: Dp = 1.2.dp,
    containerColor: Color = GlassWhiteCard,
    accentGlow: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            accentGlow?.copy(alpha = 0.6f) ?: GlassBorderTop,
            GlassPureWhite.copy(alpha = 0.8f),
            GlassBorderBottom
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = accentGlow?.copy(alpha = 0.2f) ?: Color(0x140F172A),
                spotColor = accentGlow?.copy(alpha = 0.3f) ?: Color(0x1F000000)
            )
            .clip(shape)
            .background(containerColor)
            .border(borderWidth, borderBrush, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
    ) {
        // Specular lens highlight at top of water drop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cornerRadius * 1.2f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GlassPureWhite.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Floating glassmorphic dock navigation bar at the bottom with 6 water-droplet action buttons.
 */
@Composable
fun GlassBottomNavBar(
    selectedTab: CashTab,
    onTabSelected: (CashTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        val navShape = RoundedCornerShape(28.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = navShape,
                    ambientColor = Color(0x1A0284C7),
                    spotColor = Color(0x2E0F172A)
                )
                .clip(navShape)
                .background(Color(0xE0FFFFFF)) // Semi-translucent frosted glass
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassPureWhite,
                            Color(0x66FFFFFF),
                            Color(0x330284C7)
                        )
                    ),
                    shape = navShape
                )
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CashTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                GlassNavItem(
                    tab = tab,
                    isSelected = isSelected,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GlassNavItem(
    tab: CashTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(18.dp)

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) tab.accentColor else GlassTextMuted,
        animationSpec = spring(),
        label = "iconColor"
    )

    val pillElevation by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(),
        label = "pillElevation"
    )

    Column(
        modifier = modifier
            .clip(pillShape)
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(pillElevation, pillShape)
                        .background(tab.accentColor.copy(alpha = 0.14f))
                        .border(1.dp, tab.accentColor.copy(alpha = 0.35f), pillShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = tab.title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
            ),
            color = if (isSelected) tab.accentColor else GlassTextMuted,
            maxLines = 1
        )
    }
}
