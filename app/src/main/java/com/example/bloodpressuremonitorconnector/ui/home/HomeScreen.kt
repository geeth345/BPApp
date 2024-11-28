package com.example.bloodpressuremonitorconnector.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloodpressuremonitorconnector.ui.setup.state.BleConnectionState


@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.deviceConnectionState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = when (connectionState) {
                    is BleConnectionState.Initial -> "Device not connected"
                    is BleConnectionState.Connected -> "Device connected"
                    else -> "Setup Required"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (connectionState is BleConnectionState.Connected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            Button(
                onClick = { navController.navigate("ble_setup") },
                enabled = (connectionState is BleConnectionState.Connected).not()
            ) {
                Text(if (connectionState is BleConnectionState.Connected) "Device Connected" else "Connect Device")
            }
        }
    }
}