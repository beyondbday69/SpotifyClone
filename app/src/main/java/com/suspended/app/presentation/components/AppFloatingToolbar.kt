package com.suspended.app.presentation.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.suspended.app.presentation.BottomNavItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppFloatingToolbar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    scrollBehavior: FloatingToolbarScrollBehavior,
    modifier: Modifier = Modifier
) {
    HorizontalFloatingToolbar(
        expanded = true, // We keep it expanded since scroll behavior handles visibility
        scrollBehavior = scrollBehavior,
        modifier = modifier,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            
            val containerColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
            )
            
            val contentColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
            )
            
            IconButton(
                onClick = { onItemClick(item) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                modifier = Modifier.clip(CircleShape)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label
                )
            }
        }
    }
}
