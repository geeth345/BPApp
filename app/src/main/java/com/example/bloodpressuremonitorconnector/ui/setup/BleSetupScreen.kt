package com.example.bloodpressuremonitorconnector.ui.setup

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloodpressuremonitorconnector.utils.bluetooth.state.BleConnectionState
import com.example.bloodpressuremonitorconnector.utils.bluetooth.state.BlePermissionState

@Composable
fun BleSetupScreen(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: BleSetupViewModel = viewModel(
        factory = BleSetupViewModel.Factory
    )

    val connectionState by viewModel.connectionState.collectAsState()
    val permissionState by viewModel.permissionState.collectAsState()

    // Permission launcher and bluetooth launcher remain the same
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("BleSetupScreen", "Permission results: $permissions")
        viewModel.handlePermissionResult(permissions)
    }

    val bluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermissions()
    }

    LaunchedEffect(connectionState) {
        if (connectionState is BleConnectionState.Connected) {
            onSetupComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),  // Increased padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (permissionState) {
            is BlePermissionState.RequiresPermissions -> {
                val requiredPermissions = (permissionState as BlePermissionState.RequiresPermissions).permissions

                Text(
                    text = "This app needs your permission to scan for and connect to your blood pressure sensor. After pressing the button, you'll be asked if you are okay with this.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(
                    onClick = {
                        launcher.launch(requiredPermissions.toTypedArray())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)  // Increased touch target
                ) {
                    Text(
                        text = "Grant Permissions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            is BlePermissionState.ShowRationale -> {
                Text(
                    text = "The requested permissions are required for the app to take measurements, please grant them on the next screen",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(
                    onClick = {
                        launcher.launch((permissionState as BlePermissionState.ShowRationale)
                            .permissions.toTypedArray())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Grant Permissions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            is BlePermissionState.RequiresBluetooth -> {
                Text(
                    text = "Bluetooth needs to be turned on to connect to your sensor. Please enable it on the next screen.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(
                    onClick = {
                        bluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Enable Bluetooth",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            is BlePermissionState.AllGranted -> {
                when (connectionState) {
                    is BleConnectionState.Initial -> {
                        Button(
                            onClick = { viewModel.startSetup() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Start Setup",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    is BleConnectionState.BluetoothOff -> {
                        Text(
                            text = "Please turn on Bluetooth, we use it to talk to your blood pressure sensor",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Button(
                            onClick = { viewModel.startSetup() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Retry",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    is BleConnectionState.DeviceOff -> {
                        Text(
                            text = "We couldn't find your device, please make sure it's turned on and nearby",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Button(
                            onClick = { viewModel.startSetup() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Scan Again",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    is BleConnectionState.Scanning -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),  // Larger progress indicator
                            strokeWidth = 6.dp  // Thicker stroke
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "We're looking for your device...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    is BleConnectionState.DeviceFound -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 6.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Good news! We found your device, connecting now...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    is BleConnectionState.Connected -> {
                        Text(
                            text = "You're all set up! Your device is connected and ready to take measurements.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 28.sp
                            )
                        )
                    }

                    is BleConnectionState.Error -> {
                        Text(
                            text = "Something seems to have gone wrong. Please try again, and if that doesn't work please contact support.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Button(
                            onClick = { viewModel.startSetup() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Retry",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
// preview
@Preview
@Composable
private fun BleSetupScreenPreview() {
    BleSetupScreen( onSetupComplete = {})
}