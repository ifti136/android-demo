package com.cointracker.mobile.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cointracker.mobile.data.ProfileEnvelope
import com.cointracker.mobile.ui.components.GlassCard
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalyticsScreen(envelope: ProfileEnvelope?, onBack: () -> Unit) {
    val earnings = envelope?.analytics?.earningsBreakdown ?: emptyMap()
    val spending = envelope?.analytics?.spendingBreakdown ?: emptyMap()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Analytics", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.weight(1f))
            Button(onClick = onBack) { Text("Back") }
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBox("Earnings", "+${envelope?.analytics?.totalEarnings}", Color(0xFF10B981))
                    StatBox("Spending", "${envelope?.analytics?.totalSpending}", Color(0xFFEF4444))
                    StatBox("Net", "${envelope?.analytics?.netBalance}", Color(0xFF3B82F6))
                }
            }

            item {
                Text("Balance Timeline", style = MaterialTheme.typography.titleMedium, color = Color.White)
                GlassCard(modifier = Modifier.height(200.dp)) {
                    // Placeholder for Line Chart (Using Canvas previously shown in Dashboard, adapted here)
                    val txns = envelope?.transactions ?: emptyList()
                    if (txns.isNotEmpty()) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val points = txns.sortedBy { it.date }.runningFold(0f) { sum, tx -> sum + tx.amount.toFloat() }
                            val max = points.maxOrNull() ?: 1f
                            val min = points.minOrNull() ?: 0f
                            val range = (max - min).coerceAtLeast(1f)
                            val widthPerPoint = size.width / (points.size - 1).coerceAtLeast(1)

                            for (i in 0 until points.size - 1) {
                                val x1 = i * widthPerPoint
                                val y1 = size.height - ((points[i] - min) / range * size.height)
                                val x2 = (i + 1) * widthPerPoint
                                val y2 = size.height - ((points[i + 1] - min) / range * size.height)
                                drawLine(Color(0xFF3B82F6), Offset(x1, y1), Offset(x2, y2), strokeWidth = 5f)
                            }
                        }
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No Data") }
                    }
                }
            }

            item {
                Text("Earnings Breakdown", style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (earnings.isNotEmpty()) {
                    PieChartWithLegend(data = earnings, isPositive = true)
                } else Text("No earnings data", color = Color.Gray)
            }

            item {
                Text("Spending Breakdown", style = MaterialTheme.typography.titleMedium, color = Color.White)
                if (spending.isNotEmpty()) {
                    PieChartWithLegend(data = spending, isPositive = false)
                } else Text("No spending data", color = Color.Gray)
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, color: Color) {
    GlassCard(modifier = Modifier.width(100.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleMedium, color = color)
        }
    }
}

@Composable
fun PieChartWithLegend(data: Map<String, Int>, isPositive: Boolean) {
    GlassCard {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val total = data.values.sum().toFloat()
            val colors = listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6))
            
            // Pie Chart
            Canvas(modifier = Modifier.size(100.dp)) {
                var startAngle = -90f
                data.values.forEachIndexed { index, value ->
                    val sweepAngle = (value / total) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    startAngle += sweepAngle
                }
            }
            
            Spacer(Modifier.width(24.dp))
            
            // Legend
            Column {
                data.entries.forEachIndexed { index, entry ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(12.dp), color = colors[index % colors.size], shape = MaterialTheme.shapes.small) {}
                        Spacer(Modifier.width(8.dp))
                        Text("${entry.key}: ${entry.value}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}