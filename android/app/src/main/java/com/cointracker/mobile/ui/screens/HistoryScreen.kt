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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cointracker.mobile.data.ProfileEnvelope
import com.cointracker.mobile.data.Transaction
import com.cointracker.mobile.ui.components.GlassCard

@Composable
fun HistoryScreen(
    envelope: ProfileEnvelope?,
    onDelete: (String) -> Unit,
    onEdit: (String, Int, String, String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Spacer(Modifier.height(8.dp))
        Text("History", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(envelope?.transactions ?: emptyList()) { tx ->
                TransactionRow(tx, onDelete, onEdit)
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction, onDelete: (String) -> Unit, onEdit: (String, Int, String, String) -> Unit) {
    GlassCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.date, style = MaterialTheme.typography.bodySmall)
                Text(tx.source, style = MaterialTheme.typography.titleMedium)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(if (tx.amount >= 0) "+${tx.amount}" else tx.amount.toString(), color = if (tx.amount >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)
                Row {
                    TextButton(onClick = { onEdit(tx.id, tx.amount, tx.source, tx.date) }) { Text("Edit") }
                    TextButton(onClick = { onDelete(tx.id) }) { Text("Delete") }
                }
            }
        }
    }
}
