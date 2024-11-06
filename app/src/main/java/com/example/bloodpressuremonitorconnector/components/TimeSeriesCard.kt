package com.example.bloodpressuremonitorconnector.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.ChartEntry
import java.time.LocalDateTime

data class TimeSeriesData(
    val timestamp: LocalDateTime,
    val systolic: Int,
    val diastolic: Int
)

data class TimeSeriesCardData(
    val title: String,
    val data: List<TimeSeriesData>,
    val period: String // e.g., "Last 24 Hours", "Past Week", etc.
)

@Composable
@Preview
fun TimeSeriesCard(
    cardData: TimeSeriesCardData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and period section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = cardData.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = cardData.period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Graph section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                TimeSeriesChart(data = cardData.data)
            }
        }
    }
}

@Composable
private fun TimeSeriesChart(data: List<TimeSeriesData>) {
    val chartData = remember(data) {
        data.map { dataPoint ->
            mapOf(
                "time" to dataPoint.timestamp,
                "systolic" to dataPoint.systolic,
                "diastolic" to dataPoint.diastolic
            )
        }
    }

    LineChart(
        data = chartData,
        height = 200
    ) {
        Line(
            type = "monotone",
            dataKey = "systolic",
            stroke = MaterialTheme.colorScheme.primary.toArgb().toString()
        )
        Line(
            type = "monotone",
            dataKey = "diastolic",
            stroke = MaterialTheme.colorScheme.secondary.toArgb().toString()
        )
        XAxis(dataKey = "time")
        YAxis()
        CartesianGrid(strokeDasharray = "3 3")
        Tooltip()
        Legend()
    }
}