package com.cointracker.mobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cointracker.mobile.data.AdminStats
import com.cointracker.mobile.data.AdminUserRow
import com.cointracker.mobile.data.UserSession
import com.cointracker.mobile.ui.components.GlassCard

@Composable
fun AdminScreen(
    session: UserSession?,
    stats: AdminStats?,
    users: List<AdminUserRow>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onDeleteUser: (String) -> Unit,
    onBack: () -> Unit
) {
    if (session?.role != "admin") {
        onBack(); return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onBack) { Text("Back") }
            Spacer(Modifier.weight(1f))
            Button(onClick = onRefresh, enabled = !loading) { Text("Refresh") }
        }
        Spacer(Modifier.height(8.dp))
        Text("Admin Panel", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        GlassCard { Text("Total Users: ${stats?.totalUsers ?: 0}") }
        GlassCard { Text("Total Coins Tracked: ${stats?.totalCoins ?: 0}") }
        GlassCard { Text("Total Transactions: ${stats?.totalTransactions ?: 0}") }

        Spacer(Modifier.height(12.dp))
        Text("Users", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(users) { user ->
                GlassCard {
                    Text(user.username, style = MaterialTheme.typography.titleMedium)
                    Text("Balance: ${user.balance}")
                    Text("Txns: ${user.txnCount}")
                    Text("Last Updated: ${user.lastUpdated}")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { onDeleteUser(user.userId) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}
