package com.example.bloodpressuremonitorconnector.ui.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bloodpressuremonitorconnector.data.BPReading
import com.hd.charts.LineChartView
import com.hd.charts.common.model.MultiChartDataSet
import com.hd.charts.style.ChartViewDefaults
import com.hd.charts.style.LineChartDefaults


@Composable
fun BPChartCard(
    readings: List<BPReading>,
    modifier: Modifier = Modifier,
    title: String = "Chart",
    dateFormat: String = "yyyy-MM-dd HH:mm"
) {
    // Sort readings by timestamp to ensure proper chronological order
    val sortedReadings = remember(readings) {
        readings.sortedBy { it.timestamp }
    }

    val chartDataSet = remember(sortedReadings) {
        val items = listOf(
            "Systolic" to sortedReadings.map { it.systolic.toFloat() },
            "Diastolic" to sortedReadings.map { it.diastolic.toFloat() }
        )
        MultiChartDataSet(
            items = items,
            postfix = "mmHg",
            categories = sortedReadings.map { java.text.SimpleDateFormat(dateFormat).format(java.util.Date(it.timestamp)) },
            title = title
        )
    }
    when (sortedReadings.size > 2) {
        true -> {
            LineChartView(
                dataSet = chartDataSet,
                style = LineChartDefaults.style(
                    lineColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    ),
                    dragPointVisible = false,
                    pointVisible = true,
                    pointSize = 6f,
                    chartViewStyle = ChartViewDefaults.style()
                )
            )
        }
        false -> {
            ElevatedCard() {
                Text(
                    text = "Not enough data to display chart - keep using your monitor and check back later!",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }




//    ElevatedCard(
//        modifier = modifier.fillMaxWidth()
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth()
//        ) {
//            Text(
//                text = title,
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            LineChartView(
//                dataSet = chartDataSet,
//                style = LineChartDefaults.style(
//                    lineColors = listOf(
//                        MaterialTheme.colorScheme.primary,
//                        MaterialTheme.colorScheme.secondary
//                    ),
//                    dragPointVisible = false,
//                    pointVisible = true,
//                    pointSize = 6f,
//                    chartViewStyle = ChartViewDefaults.style()
//                )
//            )
//        }
//    }
}