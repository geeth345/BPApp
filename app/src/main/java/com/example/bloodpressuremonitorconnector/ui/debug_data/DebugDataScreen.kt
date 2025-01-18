package com.example.bloodpressuremonitorconnector.ui.debug_data

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bloodpressuremonitorconnector.utils.bluetooth.state.BleConnectionState

@Composable
fun DebugDataScreen(
    navController: NavController,
    viewModel: DebugDataViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection Status Card
            ConnectionStatusCard(
                connectionState = uiState.connectionState,
                onNavigateToSetup = { navController.navigate("ble_setup") }
            )

            // Chart Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Real-time Waveform",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                    ) {
                        if (uiState.dataPoints.isEmpty()) {
                            Text(
                                text = "No data available",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            SimpleLineChart(
                                dataPoints = uiState.dataPoints,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Control Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {


                when (uiState.isRecording) {
                    true -> {
                        FilledTonalButton(
                            onClick = { viewModel.stopRecording() }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Pause")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pause")
                        }

                    }
                    false -> {
                        FilledTonalButton(
                            onClick = { viewModel.startRecording()}
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Resume")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resume")
                        }
                    }
                }

                FilledTonalButton(
                    onClick = { viewModel.clearData() }
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Data")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear")
                }
            }

            // Prediction Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Predicted Blood Pressure",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatisticItem(
                            label = "Systolic",
                            value = uiState.predictionSystolic?.toString() ?: "-"
                        )
                        StatisticItem(
                            label = "Diastolic",
                            value = uiState.predictionDiastolic?.toString() ?: "-"
                        )
                    }
                }
            }

            // Statistics Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatisticItem(
                        label = "Min",
                        value = if (uiState.minValue != Float.MAX_VALUE)
                            String.format("%.2f", uiState.minValue) else "-"
                    )
                    StatisticItem(
                        label = "Max",
                        value = if (uiState.maxValue != Float.MIN_VALUE)
                            String.format("%.2f", uiState.maxValue) else "-"
                    )
                    StatisticItem(
                        label = "Points",
                        value = uiState.dataPoints.size.toString()
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    dataPoints: List<DataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        if (dataPoints.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val maxValue = dataPoints.maxOf { it.value }
        val minValue = dataPoints.minOf { it.value }
        val valueRange = maxValue - minValue

        // Draw the line
        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            val x = (index.toFloat() / (dataPoints.size - 1)) * width
            val y = height - ((point.value - minValue) / valueRange * height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2f)
        )
    }
}

@Composable
private fun ConnectionStatusCard(
    connectionState: BleConnectionState,
    onNavigateToSetup: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (connectionState) {
                    is BleConnectionState.Connected -> "Connected"
                    is BleConnectionState.Initial -> "Not Connected"
                    is BleConnectionState.BluetoothOff -> "Bluetooth Off"
                    is BleConnectionState.DeviceOff -> "Device Not Found"
                    else -> "Connection Required"
                },
                style = MaterialTheme.typography.titleMedium
            )

            if (connectionState !is BleConnectionState.Connected) {
                Button(
                    onClick = onNavigateToSetup,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Connect Device")
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}