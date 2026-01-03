package com.cointracker.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onImportData: (String) -> Unit,
    onExportData: () -> Unit,
    onBack: () -> Unit
) {
    var goalInput by remember { mutableStateOf(envelope?.settings?.goal?.toString() ?: "13500") }
    var actionText by remember { mutableStateOf("") }
    var actionAmount by remember { mutableStateOf("") }
    var newProfileName by remember { mutableStateOf("") }
    var importJson by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.weight(1f))
            Button(onClick = onBack) { Text("Back") }
        }
        
        // Data Management (Import/Export)
        GlassCard {
            Text("Data Management", style = MaterialTheme.typography.titleMedium, color = Color(0xFF3B82F6))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExportData, modifier = Modifier.weight(1f)) { Text("Export JSON") }
                // For import, usually you'd open a file picker, here we simulated pasting JSON
            }
        }

        Spacer(Modifier.height(16.dp))

        // Goal
        GlassCard {
            Text("Goal Setting", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(goalInput, { goalInput = it }, label = { Text("Coin Goal") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { goalInput.toIntOrNull()?.let(onUpdateGoal) }, modifier = Modifier.fillMaxWidth().padding(top=8.dp)) { Text("Update Goal") }
        }

        Spacer(Modifier.height(16.dp))

        // Profiles
        GlassCard {
            Text("Create Profile", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(newProfileName, { newProfileName = it }, label = { Text("Profile Name") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { if (newProfileName.isNotBlank()) onCreateProfile(newProfileName); newProfileName = "" }, modifier = Modifier.fillMaxWidth().padding(top=8.dp)) { Text("Create") }
        }
        
        Spacer(Modifier.height(80.dp))
    }
}