package com.example.bloodpressuremonitorconnector

// MainActivity.kt
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloodpressuremonitorconnector.ui.data.DataScreen
import com.example.bloodpressuremonitorconnector.ui.home.HomeScreen
import com.example.bloodpressuremonitorconnector.ui.setup.BleSetupScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BloodPressureApp()
        }
    }
}

sealed class Screen(val route: String, val icon: @Composable () -> Unit, val label: String) {
    object Home : Screen(
        route = "home",
        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
        label = "Home"
    )
    object Data : Screen(
        route = "data",
        icon = { Icon(Icons.Filled.MonitorHeart, contentDescription = null) },
        label = "Data"
    )
    object Insights : Screen(
        route = "insights",
        icon = { Icon(Icons.Filled.Insights, contentDescription = null) },
        label = "Insights"
    )
    object Profile : Screen(
        route = "profile",
        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
        label = "Profile"
    )
    object BleSetup : Screen(
        route = "ble_setup",
        icon = { /* no icon */ },
        label = "Bluetooth Setup"
    )
}

@Preview
@Composable
fun BloodPressureApp() {
    val navController = rememberNavController()
    val navbarItems = listOf(
        Screen.Home,
        Screen.Data,
        Screen.Insights,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navbarItems.forEach { screen ->
                    NavigationBarItem(
                        icon = screen.icon,
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) { HomeScreen(navController = navController) }
                composable(Screen.Data.route) { DataScreen(navController = navController) }
                composable(Screen.Insights.route) { InsightsScreen() }
                composable(Screen.Profile.route) { ProfileScreen() }
                composable(Screen.BleSetup.route) {
                    BleSetupScreen(
                        onSetupComplete = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun InsightsScreen() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Text("Insights Screen")
    }
}

@Composable
fun ProfileScreen() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Text("Profile Screen")
    }
}