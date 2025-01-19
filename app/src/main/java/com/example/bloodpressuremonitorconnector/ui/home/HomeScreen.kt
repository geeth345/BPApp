package com.example.bloodpressuremonitorconnector.ui.home

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloodpressuremonitorconnector.navigateWithBottomBar
import com.example.bloodpressuremonitorconnector.utils.bluetooth.state.BleConnectionState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter



@OptIn(ExperimentalMaterial3Api::class)
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
    val userName by viewModel.userName.collectAsState()

    // Nice colours
    val nice_red = Color(0xFFF44336)
    val nice_blue = Color(0xFF00BCD4)
    val nice_green = Color(0xFF4CAF50)
    val nice_orange = Color(0xFFFF9800)
    val nice_grey = Color(0xFF414141)


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = "BP Connect Logo",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BPConnect",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            )
        }

    ) { innerPadding ->
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${timeBasedGreeting()},\n${userName ?: ""}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
                item {
                    // Device Status Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (connectionState is BleConnectionState.Connected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircleOutline,
                                    contentDescription = "Device Connected",
                                    tint = nice_green,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(64.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.NotInterested,
                                    contentDescription = "Device Not Connected",
                                    tint = nice_red,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(64.dp)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Device Status",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (connectionState) {
                                        is BleConnectionState.Initial -> "Device not connected"
                                        is BleConnectionState.Connected -> "Device connected"
                                        else -> "Setup Required"
                                    },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
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
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                ),
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
                                                text = latestReading?.systolic?.toString() ?: "?",
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontSize = 40.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Systolic",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = latestReading?.diastolic?.toString() ?: "?",
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontSize = 40.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Diastolic",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    val instant = Instant.ofEpochMilli(latestReading!!.timestamp)
                                    val formatter =
                                        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                                            .withZone(ZoneId.systemDefault())
                                    Text(
                                        text = "Measured at: ${formatter.format(instant)}",
                                        style = MaterialTheme.typography.bodyMedium,
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Insights,
                                contentDescription = "Insights",
                                tint = nice_orange,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(64.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Insights",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                )
                                Text(
                                    text = "View insights and trends from your measurements",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }


                    }
                }

                item {
                    // Data Sharing Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigateWithBottomBar("profile") }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalHospital,
                                contentDescription = "Profile",
                                tint = nice_blue,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(64.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Data Sharing",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                )
                                Text(
                                    text = "Share your measurements with healthcare providers",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    // Settings Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigateWithBottomBar("settings") }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = nice_grey,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(64.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                )
                                Text(
                                    text = "Configure app and device settings",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
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
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
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
}

fun timeBasedGreeting(): String {
    val currentHour = Instant.now().atZone(ZoneId.systemDefault()).hour
    return when (currentHour) {
        in 4..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}