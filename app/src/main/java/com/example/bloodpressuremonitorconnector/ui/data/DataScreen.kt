package com.example.bloodpressuremonitorconnector.ui.data


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun DataScreen(
    navController: NavController,
    viewModel: DataViewModel = viewModel(factory = DataViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val dayReadings by viewModel.dayReadings.collectAsState()
    val weekReadings by viewModel.weekReadings.collectAsState()
    val yearReadings by viewModel.yearReadings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedTimeFrame by viewModel.selectedTimeFrame.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = viewModel::loadData,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Refresh Data")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Blood Pressure Readings",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )

            // Segmented button for time frame selection
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = selectedTimeFrame == ChartTimeFrame.DAY,
                    onClick = { viewModel.setTimeFrame(ChartTimeFrame.DAY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("Day")
                }
                SegmentedButton(
                    selected = selectedTimeFrame == ChartTimeFrame.WEEK,
                    onClick = { viewModel.setTimeFrame(ChartTimeFrame.WEEK) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("Week")
                }
                SegmentedButton(
                    selected = selectedTimeFrame == ChartTimeFrame.YEAR,
                    onClick = { viewModel.setTimeFrame(ChartTimeFrame.YEAR) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("Year")
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTimeFrame) {
                        ChartTimeFrame.DAY -> BPChartCard(
                            title = "Past 24 Hours",
                            readings = dayReadings,
                            modifier = Modifier.fillMaxSize(),
                            dateFormat = "EEEE HH:mm"
                        )
                        ChartTimeFrame.WEEK -> BPChartCard(
                            title = "Past Week",
                            readings = weekReadings,
                            modifier = Modifier.fillMaxSize(),
                            dateFormat = "EEEE"
                        )
                        ChartTimeFrame.YEAR -> BPChartCard(
                            title = "Past Year",
                            readings = yearReadings,
                            modifier = Modifier.fillMaxSize(),
                            dateFormat = "MMMM YYYY"
                        )
                    }
                }
            }
        }
    }
}