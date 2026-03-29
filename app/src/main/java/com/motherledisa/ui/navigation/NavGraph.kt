package com.motherledisa.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.motherledisa.ui.animation.AnimationEditorScreen
import com.motherledisa.ui.control.ControlScreen
import com.motherledisa.ui.device.DeviceListScreen
import com.motherledisa.ui.preset.PresetLibraryScreen
import com.motherledisa.ui.orchestrate.OrchestrateScreen
import com.motherledisa.ui.sound.SoundReactiveScreen

/**
 * Main navigation graph with bottom navigation bar.
 * Implements UX-07: Navigation between screens.
 * Tab order: Devices | Control | Orchestrate | Sound | Presets (5 tabs per D-10)
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination?.hasRoute<Screen.DeviceList>() == true,
                    onClick = {
                        navController.navigate(Screen.DeviceList) {
                            // Pop up to start destination to avoid building up back stack
                            popUpTo(Screen.DeviceList) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Bluetooth, contentDescription = "Devices") },
                    label = { Text("Devices") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hasRoute<Screen.Control>() == true,
                    onClick = {
                        navController.navigate(Screen.Control()) {
                            popUpTo(Screen.DeviceList) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Control") },
                    label = { Text("Control") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hasRoute<Screen.Orchestrate>() == true,
                    onClick = {
                        navController.navigate(Screen.Orchestrate) {
                            popUpTo(Screen.DeviceList) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Hub, contentDescription = "Orchestrate") },
                    label = { Text("Orchestrate") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hasRoute<Screen.SoundReactive>() == true,
                    onClick = {
                        navController.navigate(Screen.SoundReactive) {
                            popUpTo(Screen.DeviceList) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Sound") },
                    label = { Text("Sound") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hasRoute<Screen.PresetLibrary>() == true,
                    onClick = {
                        navController.navigate(Screen.PresetLibrary()) {
                            popUpTo(Screen.DeviceList) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Presets") },
                    label = { Text("Presets") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.DeviceList,
            modifier = Modifier.padding(padding)
        ) {
            composable<Screen.DeviceList> {
                DeviceListScreen(navController = navController)
            }
            composable<Screen.Control> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.Control>()
                ControlScreen(deviceAddress = args.deviceAddress)
            }
            composable<Screen.Orchestrate> {
                OrchestrateScreen()
            }
            composable<Screen.SoundReactive> {
                SoundReactiveScreen()
            }
            composable<Screen.AnimationEditor> {
                AnimationEditorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.PresetLibrary> {
                PresetLibraryScreen(
                    onNavigateToEditor = { animationId ->
                        navController.navigate(Screen.AnimationEditor(animationId))
                    }
                )
            }
        }
    }
}
