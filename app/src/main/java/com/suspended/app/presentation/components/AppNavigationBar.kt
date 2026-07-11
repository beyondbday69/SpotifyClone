package com.suspended.app.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.suspended.app.presentation.BottomNavItem

/**
 * Standard Material 3 Expressive navigation bar
 * (https://m3.material.io/components/navigation-bar/overview), replacing
 * the previous custom black/white floating dock.
 *
 * Icon + label per destination, with a pill-shaped indicator behind the
 * selected icon. Colors are left to the NavigationBarItem defaults, which
 * pull from MaterialTheme.colorScheme (primary/secondaryContainer etc.),
 * so this automatically matches whichever app theme is active — Spotify
 * Dark, Midnight Blue, Crimson, or Dynamic. The springy selection
 * animation comes for free from MotionScheme.expressive(), already set
 * in SuspendedTheme.
 */
@Composable
fun AppNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
