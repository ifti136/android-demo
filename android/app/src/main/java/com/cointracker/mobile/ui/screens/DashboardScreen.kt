package com.cointracker.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cointracker.mobile.data.ProfileEnvelope
import com.cointracker.mobile.data.UserSession
import com.cointracker.mobile.ui.components.GlassCard
import androidx.compose.foundation.layout.width

@Composable
fun DashboardScreen(
    envelope: ProfileEnvelope?,
    onAddIncome: (Int, String, String?) -> Unit,
    onAddExpense: (Int, String, String?) -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdmin: () -> Unit,
    session: UserSession?,
    onLogout: () -> Unit,
    profiles: List<String>,
    onProfileChange: (String) -> Unit,
    loading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Dashboard", style = MaterialTheme.typography.titleLarge)
                Text("Profile: ${session?.currentProfile ?: "Default"}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (session?.role == "admin") {
                    Button(onClick = onOpenAdmin, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))) {
                        Text("Admin")
                    }
                }
                Button(onClick = onLogout, colors = ButtonDefaults.outlinedButtonColors()) { Text("Logout") }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        GlassCard {
            Text("Balance", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
            Text("${envelope?.balance ?: 0} coins", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Goal: ${envelope?.goal ?: 0} • ${envelope?.progress ?: 0}%", style = MaterialTheme.typography.bodyMedium)
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(envelope?.dashboardStats?.let { listOf(
                "Today" to it.today,
                "Week" to it.week,
                "Month" to it.month
            ) } ?: emptyList()) { (label, value) ->
                GlassCard(modifier = Modifier.width(140.dp)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
                    Text("$value", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        GlassCard {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(envelope?.settings?.quickActions ?: emptyList()) { action ->
                    Button(
                        onClick = {
                            if (action.isPositive) onAddIncome(action.value, action.text, null)
                            else onAddExpense(action.value, action.text, null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (action.isPositive) Color(0xFF22C55E) else Color(0xFFEF4444))
                    ) { Text(action.text) }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        GlassCard {
            Text("Achievements", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (envelope?.achievements.isNullOrEmpty()) {
                Text("No achievements yet. Keep going!", color = Color.LightGray)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(envelope?.achievements ?: emptyList()) { a ->
                        GlassCard(modifier = Modifier.width(180.dp)) {
                            Text(a.icon + " " + a.name, fontWeight = FontWeight.Bold)
                            Text(a.desc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(modifier = Modifier.weight(1f), onClick = onOpenAnalytics) { Text("Analytics") }
            Button(modifier = Modifier.weight(1f), onClick = onOpenHistory) { Text("History") }
            Button(modifier = Modifier.weight(1f), onClick = onOpenSettings) { Text("Settings") }
        }
    }
}
