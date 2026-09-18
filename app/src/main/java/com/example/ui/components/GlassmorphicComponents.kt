package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWaterBlue
import com.example.ui.theme.GlassWaterBlueBg
import com.example.ui.theme.GlassWhiteCard
import com.example.ui.theme.SymphonixBlue
import com.example.ui.theme.SymphonixDeepBlue
import com.example.ui.theme.SymphonixLightBlue

/**
 * Navigation tabs matching the app structure
 */
enum class CashTab(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    DASHBOARD("Aperçu", Icons.Default.Dashboard, SymphonixBlue),
    ENCAISSEMENT("Aliment.", Icons.Default.AddCircle, GlassEmeraldGreen),
    DECAISSEMENT("Décaisse.", Icons.Default.RemoveCircle, GlassCoralRed),
    HISTORIQUE("Historique", Icons.Default.History, SymphonixDeepBlue),
    CLOTURE("Clôture", Icons.Default.Lock, Color(0xFF0D9488)),
    PARAMETRES("Paramètres", Icons.Default.Settings, Color(0xFF475569))
}

/**
 * Dynamic organic liquid water droplet background with smooth breathing wave animations.
 */
@Composable
fun WaterDropletBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waterInfinite")

    // Slow ambient breathing phase for water droplets
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1.05f,
        targetValue = 0.93f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F8FB),
                        Color(0xFFEBF5FA),
                        Color(0xFFF0FDF8),
                        Color(0xFFF8FAFC)
                    )
                )
            )
    ) {
        // Decorative fluid animated water shapes in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Organic Water Drop 1 (Top right - Cyan Blue) with breathing scale
            val r1 = width * 0.55f * pulse1
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3838BDF8),
                        Color(0x1838BDF8),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.12f + waveOffset * 0.3f),
                    radius = r1
                ),
                center = Offset(width * 0.85f, height * 0.12f + waveOffset * 0.3f),
                radius = r1
            )

            // Organic Water Drop 2 (Middle left - Emerald Green)
            val r2 = width * 0.6f * pulse2
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3034D399),
                        Color(0x1234D399),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.1f, height * 0.42f - waveOffset * 0.2f),
                    radius = r2
                ),
                center = Offset(width * 0.1f, height * 0.42f - waveOffset * 0.2f),
                radius = r2
            )

            // Organic Water Drop 3 (Bottom right - Soft Rose / Coral)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x22F43F5E),
                        Color(0x0AF43F5E),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.9f, height * 0.75f + waveOffset * 0.4f),
                    radius = width * 0.5f * pulse1
                ),
                center = Offset(width * 0.9f, height * 0.75f + waveOffset * 0.4f),
                radius = width * 0.5f * pulse1
            )

            // Organic Water Drop 4 (Symphonix Blue Top Left ambient reflection)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x20146BFF),
                        Color(0x08146BFF),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.08f),
                    radius = width * 0.45f * pulse2
                ),
                center = Offset(width * 0.15f, height * 0.08f),
                radius = width * 0.45f * pulse2
            )

            // Fluid water curve with gentle wave undulating motion
            val wavePath = Path().apply {
                moveTo(0f, height * 0.28f + waveOffset)
                cubicTo(
                    width * 0.35f, height * 0.22f - waveOffset,
                    width * 0.65f, height * 0.34f + waveOffset,
                    width, height * 0.26f - waveOffset
                )
                lineTo(width, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x180284C7),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height * 0.32f
                )
            )
        }

        content()
    }
}

/**
 * Ultra-clear water droplet glass card with spring physics on press, specular highlights
 * and curved refraction borders.
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "cardScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 2.dp else 6.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "cardElevation"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            accentGlow?.copy(alpha = 0.6f) ?: GlassBorderTop,
            GlassPureWhite.copy(alpha = 0.85f),
            GlassBorderBottom
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = accentGlow?.copy(alpha = 0.2f) ?: Color(0x140F172A),
                spotColor = accentGlow?.copy(alpha = 0.3f) ?: Color(0x1F000000)
            )
            .clip(shape)
            .background(containerColor)
            .border(borderWidth, borderBrush, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
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
                            GlassPureWhite.copy(alpha = 0.55f),
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
 * Floating glassmorphic dock navigation bar at the bottom with responsive tablet centering
 * and a smooth sliding liquid bubble transition across tabs.
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
        val navShape = RoundedCornerShape(26.dp)
        val tabs = CashTab.values()

        Box(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = navShape,
                    ambientColor = SymphonixBlue.copy(alpha = 0.12f),
                    spotColor = SymphonixDeepBlue.copy(alpha = 0.18f)
                )
                .clip(navShape)
                .background(Color(0xF6FFFFFF))
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xD0FFFFFF),
                            SymphonixBlue.copy(alpha = 0.18f)
                        )
                    ),
                    shape = navShape
                )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                val itemWidth = maxWidth / tabs.size

                // Liquid bubble that slides smoothly between tabs
                val animatedBubbleX by animateDpAsState(
                    targetValue = itemWidth * selectedTab.ordinal,
                    animationSpec = spring(
                        dampingRatio = 0.74f,
                        stiffness = 380f
                    ),
                    label = "bubbleSlide"
                )

                val bubbleColor by animateColorAsState(
                    targetValue = selectedTab.accentColor,
                    animationSpec = tween(durationMillis = 250),
                    label = "bubbleColor"
                )

                // The sliding active bubble pill
                Box(
                    modifier = Modifier
                        .offset(x = animatedBubbleX)
                        .width(itemWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    bubbleColor.copy(alpha = 0.20f),
                                    bubbleColor.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    bubbleColor.copy(alpha = 0.45f)
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    // Specular gleam at top of sliding bubble
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.65f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Interactive tab items placed over the sliding track
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        GlassNavItem(
                            tab = tab,
                            isSelected = isSelected,
                            onClick = { onTabSelected(tab) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isSelected -> 1.06f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
        label = "navItemScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) tab.accentColor else Color(0xFF64748B),
        animationSpec = tween(durationMillis = 220),
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) tab.accentColor else Color(0xFF64748B),
        animationSpec = tween(durationMillis = 220),
        label = "textColor"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
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
                letterSpacing = (-0.2).sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = textColor,
            maxLines = 1
        )
    }
}
