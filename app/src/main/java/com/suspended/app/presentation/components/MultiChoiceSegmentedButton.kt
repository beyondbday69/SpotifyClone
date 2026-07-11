package com.suspended.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Multi-select segmented button row (Walk / Ride / Drive), following the
 * M3 SegmentedButton pattern. Colors are pulled from MaterialTheme.colorScheme
 * so it matches whichever app theme is active (Spotify Dark, Midnight Blue,
 * Crimson, Dynamic) — same approach as ExpressiveSlider and AppNavigationBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiChoiceSegmentedButton(modifier: Modifier = Modifier) {
    val selectedOptions = remember {
        mutableStateListOf(false, false, false)
    }
    val options = listOf("Walk", "Ride", "Drive")

    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.primary,
        activeContentColor = MaterialTheme.colorScheme.onPrimary,
        activeBorderColor = MaterialTheme.colorScheme.primary,
        inactiveContainerColor = MaterialTheme.colorScheme.surface,
        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        inactiveBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    )

    MultiChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                checked = selectedOptions[index],
                onCheckedChange = {
                    selectedOptions[index] = !selectedOptions[index]
                },
                colors = colors,
                icon = { SegmentedButtonDefaults.Icon(selectedOptions[index]) },
                label = {
                    when (label) {
                        "Walk" -> Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = "Directions Walk"
                        )
                        "Ride" -> Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = "Directions Bus"
                        )
                        "Drive" -> Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Directions Car"
                        )
                    }
                }
            )
        }
    }
}
