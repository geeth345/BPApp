package com.example.bloodpressuremonitorconnector.ui.data


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bloodpressuremonitorconnector.db.BPReading
import com.example.bloodpressuremonitorconnector.ui.data.BPChartCard

@Composable
fun DataScreen(
    navController: NavController,
    viewModel: DataViewModel = viewModel(factory = DataViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val readings by viewModel.readings.collectAsState()

    // Load data when the screen is first displayed
    val context = LocalContext.current
    LaunchedEffect(context) {
        viewModel.loadData(context)
    }

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Blood Pressure Readings",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            item {
                BPChartCard(
                    title = "Past Day",
                    readings = readings,
                    modifier = Modifier.fillMaxSize()
                )
            }
            item {
                BPChartCard(
                    title = "Past Week",
                    readings = readings,
                    modifier = Modifier.fillMaxSize()
                )
            }
            item {
                BPChartCard(
                    title = "Past Year",
                    readings = readings,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}