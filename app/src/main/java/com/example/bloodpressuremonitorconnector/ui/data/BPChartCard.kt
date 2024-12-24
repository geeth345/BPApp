package com.example.bloodpressuremonitorconnector.ui.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bloodpressuremonitorconnector.data.BPReading
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.Locale

// A Composable function that displays a card with a graph of blood pressure readings
// Using Vico
@Composable
fun BPChartCard(
    readings: List<BPReading>,
    modifier: Modifier = Modifier,
    title: String = "Chart"
) {
    // Sort readings by timestamp to ensure proper chronological order
    val sortedReadings = remember(readings) {
        readings.sortedBy { it.timestamp }
    }

    // Create chart entries for systolic and diastolic readings
    val chartEntryModel = remember(sortedReadings) {
        val systolicEntries = sortedReadings.mapIndexed { index, reading ->
            entryOf(index.toFloat(), reading.systolic.toFloat())
        }
        val diastolicEntries = sortedReadings.mapIndexed { index, reading ->
            entryOf(index.toFloat(), reading.diastolic.toFloat())
        }
        entryModelOf(systolicEntries, diastolicEntries)
    }

    // Date formatter for x-axis labels
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        // elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Chart(
                chart = lineChart(),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis()
            )

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "Systolic",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Text(
                        text = "Diastolic",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}