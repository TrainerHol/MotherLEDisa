package com.motherledisa.ui.orchestrate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.domain.model.Animation

/**
 * Per-tower animation assignment for independent mode.
 * Per D-05: Dropdown per tower to select animation/preset. Compact, fits DevicePicker pattern.
 * Per MULTI-06: Each tower controlled separately.
 */
@Composable
fun IndependentTowerConfig(
    tower: ConnectedTower,
    animations: List<Animation>,
    selectedAnimationId: Long?,
    onAnimationSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAnimation = animations.find { it.id == selectedAnimationId }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tower.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(120.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedAnimation?.name ?: "None",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // "None" option
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        onAnimationSelected(null)
                        expanded = false
                    }
                )
                // Animation options
                animations.forEach { animation ->
                    DropdownMenuItem(
                        text = { Text(animation.name) },
                        onClick = {
                            onAnimationSelected(animation.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * List of all towers with their animation assignments for independent mode.
 */
@Composable
fun IndependentTowerConfigList(
    towers: List<ConnectedTower>,
    animations: List<Animation>,
    towerAnimations: Map<String, Long>,
    onAnimationSelected: (towerAddress: String, animationId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Per-Tower Animations",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        towers.forEach { tower ->
            IndependentTowerConfig(
                tower = tower,
                animations = animations,
                selectedAnimationId = towerAnimations[tower.address],
                onAnimationSelected = { animationId ->
                    onAnimationSelected(tower.address, animationId)
                }
            )
        }
        if (towers.isEmpty()) {
            Text(
                text = "No towers connected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
