package com.suspended.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.suspended.app.presentation.BottomNavItem

/**
 * Sleek, minimal black & white dock bar.
 *
 * Replaces the previous fully-rounded "half circle" pill toolbar with a
 * flat black bar with subtle 10dp rounded corners. The selected item is
 * shown as a small white pill with a black icon; unselected icons are
 * plain white on black.
 *
 * Note: this flat Surface no longer participates in the M3 Expressive
 * floating-toolbar scroll-to-hide system (that behavior is baked into
 * HorizontalFloatingToolbar's own pill shape, which we intentionally moved
 * away from). `scrollBehavior` is kept in the signature so the call site
 * doesn't need to change; wire it back up if you want hide-on-scroll
 * on top of the new look.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppFloatingToolbar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    scrollBehavior: FloatingToolbarScrollBehavior,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.Black,
        contentColor = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.screen.route

                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.Transparent,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                    label = "dockItemBg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.Black else Color.White,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                    label = "dockItemTint"
                )

                IconButton(
                    onClick = { onItemClick(item) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                }
            }
        }
    }
}
