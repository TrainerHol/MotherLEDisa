package com.motherledisa.ui.control.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.motherledisa.domain.model.Effect
import com.motherledisa.domain.model.EffectCategory
import com.motherledisa.ui.theme.Background
import com.motherledisa.ui.theme.OnSurfaceSecondary
import com.motherledisa.ui.theme.PrimaryAccent

/**
 * Effects selection section with category grouping.
 *
 * Per D-18: Full hardware effects menu exposed (~20+ effects)
 * Per D-19: Effects displayed as scrollable vertical list with icon per row
 * Per D-22: Effects grouped by category with section headers
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EffectsSection(
    effectsByCategory: Map<EffectCategory, List<Effect>>,
    activeEffect: Effect?,
    onEffectSelected: (Effect) -> Unit,
    onClearEffect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Effects",
                style = MaterialTheme.typography.titleMedium
            )

            if (activeEffect != null) {
                TextButton(onClick = onClearEffect) {
                    Text("Clear")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // D-22: Effects grouped by category with section headers
        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            effectsByCategory.forEach { (category, effects) ->
                // Category header (sticky)
                stickyHeader {
                    Surface(
                        color = Background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // D-19: Effect items
                items(effects) { effect ->
                    EffectItem(
                        effect = effect,
                        isActive = effect == activeEffect,
                        onClick = { onEffectSelected(effect) }
                    )
                }
            }
        }
    }
}

/**
 * Individual effect list item.
 */
@Composable
private fun EffectItem(
    effect: Effect,
    isActive: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(effect.name)
        },
        leadingContent = {
            // Icon based on category
            Icon(
                imageVector = when (effect.category) {
                    EffectCategory.FADE -> Icons.Default.Gradient
                    EffectCategory.JUMP -> Icons.Default.Bolt
                    EffectCategory.BREATHE -> Icons.Default.Air
                    EffectCategory.STROBE -> Icons.Default.FlashOn
                    EffectCategory.MULTI_COLOR -> Icons.Default.Palette
                    EffectCategory.GRADIENT -> Icons.Default.Gradient
                    else -> Icons.Default.LightMode
                },
                contentDescription = null,
                tint = if (isActive) PrimaryAccent else OnSurfaceSecondary
            )
        },
        trailingContent = {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = PrimaryAccent
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isActive)
                PrimaryAccent.copy(alpha = 0.1f)
            else
                Color.Transparent
        ),
        modifier = Modifier.clickable(onClick = onClick)
    )
}
