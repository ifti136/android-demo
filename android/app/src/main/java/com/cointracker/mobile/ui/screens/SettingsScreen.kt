package com.cointracker.mobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cointracker.mobile.data.ProfileEnvelope
import com.cointracker.mobile.data.QuickAction
import com.cointracker.mobile.ui.components.GlassCard

@Composable
fun SettingsScreen(
    envelope: ProfileEnvelope?,
    profiles: List<String>,
    onUpdateGoal: (Int) -> Unit,
    onAddQuickAction: (QuickAction) -> Unit,
    onDeleteQuickAction: (Int) -> Unit,
    onCreateProfile: (String) -> Unit,
    onBack: () -> Unit
) {
    val goalInput = remember { mutableStateOf(envelope?.settings?.goal?.toString() ?: "13500") }
    val actionText = remember { mutableStateOf("") }
    val actionAmount = remember { mutableStateOf("") }
    val actionType = remember { mutableStateOf(true) }
    val newProfileName = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Spacer(Modifier.height(8.dp))
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        GlassCard {
            Text("Goal", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(goalInput.value, { goalInput.value = it }, label = { Text("Coin goal") })
            Spacer(Modifier.height(8.dp))
            Button(onClick = { goalInput.value.toIntOrNull()?.let(onUpdateGoal) }) { Text("Update Goal") }
        }

        GlassCard {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(actionText.value, { actionText.value = it }, label = { Text("Label") })
            OutlinedTextField(actionAmount.value, { actionAmount.value = it }, label = { Text("Amount") })
            Row {
                TextButton(onClick = { actionType.value = true }) { Text(if (actionType.value) "Income ✓" else "Income") }
                TextButton(onClick = { actionType.value = false }) { Text(if (!actionType.value) "Expense ✓" else "Expense") }
            }
            Button(onClick = {
                val amt = actionAmount.value.toIntOrNull() ?: return@GlassCard
                onAddQuickAction(QuickAction(actionText.value, amt, actionType.value))
            }) { Text("Add Action") }
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                itemsIndexed(envelope?.settings?.quickActions ?: emptyList()) { idx, qa ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("${qa.text} (${if (qa.isPositive) "+" else "-"}${qa.value})", modifier = Modifier.weight(1f))
                        TextButton(onClick = { onDeleteQuickAction(idx) }) { Text("Remove") }
                    }
                }
            }
        }

        GlassCard {
            Text("Profiles", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(newProfileName.value, { newProfileName.value = it }, label = { Text("New profile name") })
            Button(onClick = { if (newProfileName.value.isNotBlank()) onCreateProfile(newProfileName.value.trim()) }) { Text("Create Profile") }
            Spacer(Modifier.height(8.dp))
            Text("Available: ${profiles.joinToString()}")
        }
    }
}
