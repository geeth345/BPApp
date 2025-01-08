package com.example.bloodpressuremonitorconnector.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloodpressuremonitorconnector.navigateWithBottomBar
import com.example.bloodpressuremonitorconnector.utils.bluetooth.state.BleConnectionState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory
    ),
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.deviceConnectionState.collectAsState()

    val latestReading by viewModel.latestReading.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
            item {
                // Device Status Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Device Status",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (connectionState) {
                                is BleConnectionState.Initial -> "Device not connected"
                                is BleConnectionState.Connected -> "Device connected"
                                else -> "Setup Required"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (connectionState is BleConnectionState.Connected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { navController.navigate("ble_setup") },
                            enabled = (connectionState is BleConnectionState.Connected).not(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (connectionState is BleConnectionState.Connected)
                                    "Device Connected"
                                else "Connect Device"
                            )
                        }
                    }
                }
            }

            item {
                // Latest Measurements Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Latest Measurements",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        when (latestReading) {
                            null -> {
                                Text(
                                    text = "No measurements available, please connect a device or" +
                                            "login to load data",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            else -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = latestReading?.systolic?.toString()?: "?",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Systolic",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = latestReading?.diastolic?.toString()?: "?",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Diastolic",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                val instant = Instant.ofEpochMilli(latestReading!!.timestamp)
                                val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                                    .withZone(ZoneId.systemDefault())
                                Text(
                                    text = "Measured at: ${formatter.format(instant)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Insights text card
                ElevatedCard(onClick = { navController.navigateWithBottomBar("insights") }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "View insights and trends from your measurements",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                }
            }

            item {
                // Data Sharing Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* Navigate to sharing screen */ }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Share Data",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Share your measurements with healthcare providers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                // Settings Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigateWithBottomBar("settings") }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Configure app and device settings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (viewModel.debugModeState.value) {
                item {
                    // Debug screen card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("debug_data") }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Debug Data",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "View data stream from device for debugging purposes. " +
                                        "This option is available as the app is in debug mode.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                    }
                }

                item {
                    // load mock data from csv
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.loadMockData() }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Load Mock Data",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Tap to load mock data into the database for testing purposes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                    }
                }
            }

        }
    }
}



//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeTopBar() {
//    TopAppBar(
//        title = {
//            Text(
//                text = "Blood Pressure Monitor",
//                style = MaterialTheme.typography.titleLarge,
//                textAlign = TextAlign.Center
//            )
//        },
//        colors = TopAppBarDefaults.topAppBarColors(
//            containerColor = MaterialTheme.colorScheme.primaryContainer,
//            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//        )
//    )
//}