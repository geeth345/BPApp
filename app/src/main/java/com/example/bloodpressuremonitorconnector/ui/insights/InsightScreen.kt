package com.example.bloodpressuremonitorconnector.ui.insights

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun InsightsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: InsightsViewModel = viewModel(factory = InsightsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Health Insights",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item { CvdRiskCard(cvdRiskScore = uiState.cvdRiskScore) }
                item { StressLevelCard(stressScore = uiState.stressScore) }
                item {
                    BPAverageCard(
                        avgSystolic = uiState.averageSystolic,
                        avgDiastolic = uiState.averageDiastolic
                    )
                }
                items(uiState.insights) { insight -> InsightCard(insight = insight) }
            }
        }
    }
}

@Composable
fun CvdRiskCard(cvdRiskScore: Int) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)  // Increased padding
        ) {
            Text(
                text = "Cardiovascular Health Assessment",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))  // Increased spacing

            LinearProgressIndicator(
                progress = { cvdRiskScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),  // Thicker progress bar
                color = when {
                    cvdRiskScore < 30 -> MaterialTheme.colorScheme.primary
                    cvdRiskScore < 60 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.secondary
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Assessment Score: $cvdRiskScore%",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = getRiskDescription(cvdRiskScore),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun StressLevelCard(stressScore: Int) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Stress Level Indicator",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { stressScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = when {
                    stressScore < 30 -> MaterialTheme.colorScheme.primary
                    stressScore < 60 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.secondary
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Current Level: $stressScore%",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = getStressDescription(stressScore),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun BPAverageCard(avgSystolic: Int, avgDiastolic: Int) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Blood Pressure Overview",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = avgSystolic.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Systolic",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = avgDiastolic.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Diastolic",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp
                        )
                    )
                }
            }

            Text(
                text = getBPDescription(avgSystolic, avgDiastolic),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

@Composable
fun InsightCard(insight: BPInsight) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (insight.actionNeeded) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Action Suggested",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)  // Larger icon
                )
            }

            Column {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = when (insight.severity) {
                        InsightSeverity.LOW -> MaterialTheme.colorScheme.primary
                        InsightSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
                        InsightSeverity.HIGH -> MaterialTheme.colorScheme.secondary
                    }
                )
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



private fun getRiskDescription(score: Int): String = when {
    score < 30 -> "Your cardiovascular health indicators are looking good. Keep up your healthy habits!"
    score < 60 -> "Consider discussing your cardiovascular health with your GP at your next check-up."
    else -> "We recommend scheduling an appointment with your GP to review your cardiovascular health."
}

private fun getStressDescription(score: Int): String = when {
    score < 30 -> "Your readings suggest low stress levels. Keep maintaining a balanced lifestyle!"
    score < 60 -> "Moderate variability in readings. Consider discussing stress management with your GP."
    else -> "Your readings show higher variability. This would be good to discuss with your healthcare provider."
}

private fun getBPDescription(systolic: Int, diastolic: Int): String = when {
    systolic < 120 && diastolic < 80 ->
        "Your blood pressure is in a healthy range."
    systolic < 130 && diastolic < 85 ->
        "Your blood pressure is slightly above optimal range. Consider discussing this at your next check-up."
    else ->
        "Your blood pressure readings suggest scheduling a review with your GP would be beneficial."
}