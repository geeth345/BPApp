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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    // Permission launcher
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Log the permissions being handled
        Log.d("BleSetupScreen", "Permission results: $permissions")
        viewModel.handlePermissionResult(permissions)
    }

    // Bluetooth enable launcher
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (permissionState) {
            is BlePermissionState.RequiresPermissions -> {
                val requiredPermissions = (permissionState as BlePermissionState.RequiresPermissions).permissions
                Log.d("BleSetupScreen", "Requesting permissions: $requiredPermissions")

                Text("This app needs your permission to scan for and connect to your blood pressure sensor. After pressing the button, you'll be asked if you are okay with this.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        launcher.launch(requiredPermissions.toTypedArray())
                    }
                ) {
                    Text("Grant Permissions")
                }
            }
            is BlePermissionState.ShowRationale -> {
                Text("The requested permissions are required for the app to take measurements, please grant them on the next screen")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        launcher.launch((permissionState as BlePermissionState.ShowRationale)
                            .permissions.toTypedArray())
                    }
                ) {
                    Text("Grant Permissions")
                }
            }
            is BlePermissionState.RequiresBluetooth -> {
                Text("Bluetooth needs to be turned on to connect to your sensor. Please enable it on the next screen.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        bluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    }
                ) {
                    Text("Enable Bluetooth")
                }
            }
            is BlePermissionState.AllGranted -> {
                // Show the regular BLE setup UI when all permissions are granted
                when (connectionState) {
                    is BleConnectionState.Initial -> {
                        Button(onClick = { viewModel.startSetup() }) {
                            Text("Start Setup")
                        }
                    }

                    is BleConnectionState.BluetoothOff -> {
                        Text("Please turn on Bluetooth, we use it to talk to your blood pressure sensor")
                        Button(onClick = { viewModel.startSetup() }) {
                            Text("Retry")
                        }
                    }

                    is BleConnectionState.DeviceOff -> {
                        Text("We couldn't find your device, please make sure it's turned on and nearby")
                        Button(onClick = { viewModel.startSetup() }) {
                            Text("Scan Again")
                        }
                    }

                    is BleConnectionState.Scanning -> {
                        CircularProgressIndicator()
                        Text("We're looking for your device...")
                    }

                    is BleConnectionState.DeviceFound -> {
                        CircularProgressIndicator()
                        Text("Good news! We found your device, connecting now...")
                    }

                    is BleConnectionState.Connected -> {
                        Text("You're all set up! Your device is connected and ready to take measurements.")
                    }

                    is BleConnectionState.Error -> {
                        Text("Something seems to have gone wrong. Please try again, and if that doesn't work please contact support.")
                        //Text((connectionState as BleConnectionState.Error).message)
                        Button(onClick = { viewModel.startSetup() }) {
                            Text("Retry")
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