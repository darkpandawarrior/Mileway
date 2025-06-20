package com.miletracker.core.ui.components.bottombar

import android.view.HapticFeedbackConstants
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data class for bottom navigation items with badge support.
 * Supports both ImageVector icons and drawable resources.
 */
data class EnhancedBottomNavItem(
    val label: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null,
    @param:DrawableRes val selectedDrawable: Int? = null,
    @param:DrawableRes val unselectedDrawable: Int? = null,
    val badgeCount: Int? = null,
    val isHome: Boolean = false
)

/**
 * A modern bottom navigation bar with a smooth sliding indicator pill.
 */
@Composable
fun EnhancedBottomBar(
    items: List<EnhancedBottomNavItem>,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit,
    onItemReselected: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 3.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(80.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val tabWidth = maxWidth / items.size

            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedItemIndex,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = Spring.StiffnessLow
                ),
                label = "indicator_offset"
            )

            // 1. Sliding Indicator Layer (Behind the icons)
            Box(
                modifier = Modifier
                    .width(tabWidth)
                    .fillMaxHeight()
                    .offset(x = indicatorOffset),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = " ", fontSize = 10.sp)
                }
            }

            // 2. Foreground Icons & Text Layer
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedItemIndex
                    SmoothNavItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) {
                                onItemReselected?.invoke(index)
                            } else {
                                onItemSelected(index)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * A navigation item that animates text/icon colors, but leaves the background
 * to be handled by the sliding indicator in the parent.
 */
@Composable
private fun SmoothNavItem(
    item: EnhancedBottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "content_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onClick()
                }
            )
            .padding(vertical = 8.dp)
    ) {
        BadgedBox(
            badge = {
                if (item.badgeCount != null && item.badgeCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text(
                            text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                val useVector = item.selectedIcon != null && item.unselectedIcon != null
                val useDrawable = item.selectedDrawable != null && item.unselectedDrawable != null

                if (useVector) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon!! else item.unselectedIcon!!,
                        contentDescription = item.label,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(scale),
                        tint = contentColor
                    )
                } else if (useDrawable) {
                    Icon(
                        painter = painterResource(id = if (isSelected) item.selectedDrawable!! else item.unselectedDrawable!!),
                        contentDescription = item.label,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(scale),
                        tint = contentColor
                    )
                } else {
                    Box(Modifier.size(24.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
