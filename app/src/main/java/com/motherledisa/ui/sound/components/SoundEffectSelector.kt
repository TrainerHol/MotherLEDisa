package com.motherledisa.ui.sound.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.motherledisa.domain.model.SoundEffect

/**
 * Selector for sound-reactive effects (0x80-0x87).
 * Per D-08: Expose all 8 sound-reactive effects from protocol.
 *
 * @param selectedEffect Currently selected effect
 * @param onEffectSelected Called when user selects an effect
 * @param enabled Whether selector is enabled (disabled when sound mode off)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoundEffectSelector(
    selectedEffect: SoundEffect,
    onEffectSelected: (SoundEffect) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Sound Effect",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoundEffect.all.forEach { effect ->
                FilterChip(
                    selected = effect == selectedEffect,
                    onClick = { onEffectSelected(effect) },
                    label = { Text(effect.displayName) },
                    enabled = enabled,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
