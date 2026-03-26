package com.motherledisa.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
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
import com.motherledisa.ui.control.ControlScreen
import com.motherledisa.ui.device.DeviceListScreen

/**
 * Main navigation graph with bottom navigation bar.
 * Implements UX-07: Navigation between Device List and Control screens.
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
        }
    }
}
