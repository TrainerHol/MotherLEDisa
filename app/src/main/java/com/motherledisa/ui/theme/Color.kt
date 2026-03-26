package com.motherledisa.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MotherLEDisa color palette from UI-SPEC.md
 * Dark theme with dual accent colors for LED tower theming.
 */

// Surfaces (60% dominant)
val Background = Color(0xFF121212)
val Surface = Color(0xFF1E1E1E)

// Accents (10% primary)
val PrimaryAccent = Color(0xFF7C4DFF)  // Purple - connected states, active effects
val SecondaryAccent = Color(0xFFFFEB3B) // Yellow - power ON, scanning indicator

// Text on surface
val OnSurfacePrimary = Color(0xFFFFFFFF)  // 87% opacity for primary text
val OnSurfaceSecondary = Color(0xFFB3B3B3) // 60% opacity for labels, secondary text
val OnSurfaceDisabled = Color(0xFF666666)  // 38% opacity for disabled text

// Status colors
val Destructive = Color(0xFFCF6679)  // Material Dark error - disconnect action
