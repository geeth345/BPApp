package com.example.bloodpressuremonitorconnector.ui.settings


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bloodpressuremonitorconnector.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Debug Mode Toggle
            ListItem(
                headlineContent = { Text("Debug Mode") },
                supportingContent = { Text("Enable additional debugging information") },
                trailingContent = {
                    Switch(
                        checked = uiState.debugMode,
                        onCheckedChange = { viewModel.setDebugMode(it) }
                    )
                }
            )

            HorizontalDivider()

            // Measurement Interval Selector
            var showIntervalDropdown by remember { mutableStateOf(false) }
            ListItem(
                headlineContent = { Text("Measurement Interval") },
                supportingContent = { Text("Time between blood pressure measurements") },
                trailingContent = {
                    Box {
                        TextButton(onClick = { showIntervalDropdown = true }) {
                            Text("${uiState.measurementInterval} min")
                        }
                        DropdownMenu(
                            expanded = showIntervalDropdown,
                            onDismissRequest = { showIntervalDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("15 minutes") },
                                onClick = {
                                    viewModel.setMeasurementInterval(15)
                                    showIntervalDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("30 minutes") },
                                onClick = {
                                    viewModel.setMeasurementInterval(30)
                                    showIntervalDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("1 hour") },
                                onClick = {
                                    viewModel.setMeasurementInterval(60)
                                    showIntervalDropdown = false
                                }
                            )
                        }
                    }
                }
            )

            HorizontalDivider()

            // Notifications Toggle
            ListItem(
                headlineContent = { Text("Notifications") },
                supportingContent = { Text("Enable measurement reminders") },
                trailingContent = {
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }
            )

        }
    }
}