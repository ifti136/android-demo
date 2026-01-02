package com.cointracker.mobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cointracker.mobile.data.ProfileEnvelope
import com.cointracker.mobile.ui.components.GlassCard

@Composable
fun AnalyticsScreen(envelope: ProfileEnvelope?, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Spacer(Modifier.height(8.dp))
        Text("Analytics", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        GlassCard { Text("Total Earnings: ${envelope?.analytics?.totalEarnings ?: 0}") }
        GlassCard { Text("Total Spending: ${envelope?.analytics?.totalSpending ?: 0}") }
        GlassCard { Text("Net Balance: ${envelope?.analytics?.netBalance ?: 0}") }
        Spacer(Modifier.height(8.dp))
        Text("Earnings Breakdown", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(envelope?.analytics?.earningsBreakdown?.entries?.toList() ?: emptyList()) { entry ->
                GlassCard { Text("${entry.key}: ${entry.value}") }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Spending Breakdown", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(envelope?.analytics?.spendingBreakdown?.entries?.toList() ?: emptyList()) { entry ->
                GlassCard { Text("${entry.key}: ${entry.value}") }
            }
        }
    }
}
