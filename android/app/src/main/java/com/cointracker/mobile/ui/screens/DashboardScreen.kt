package com.cointracker.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cointracker.mobile.data.ProfileEnvelope
import com.cointracker.mobile.data.UserSession
import com.cointracker.mobile.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    envelope: ProfileEnvelope?,
    session: UserSession?,
    loading: Boolean,
    profiles: List<String>,
    onAddIncome: (Int, String, String?) -> Unit,
    onAddExpense: (Int, String, String?) -> Unit,
    onProfileChange: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    var showProfileMenu by remember { mutableStateOf(false) }
    var addAmount by remember { mutableStateOf("") }
    var addSource by remember { mutableStateOf("Other") }
    var spendAmount by remember { mutableStateOf("") }
    var spendCategory by remember { mutableStateOf("Other") }

    val sources = listOf("Event Reward", "Login", "Daily Games", "Campaign Reward", "Ads", "Achievements", "Other")
    val categories = listOf("Box Draw", "Manager Purchase", "Pack Purchase", "Store Purchase", "Other")

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar with Profile Switcher
        GlassCard(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Coin Tracker", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Box {
                        Row(modifier = Modifier.clickable { showProfileMenu = true }, verticalAlignment = Alignment.CenterVertically) {
                            Text("Profile: ${session?.currentProfile ?: "Default"}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Change Profile", tint = Color.Gray)
                        }
                        DropdownMenu(expanded = showProfileMenu, onDismissRequest = { showProfileMenu = false }) {
                            profiles.forEach { profile ->
                                DropdownMenuItem(
                                    text = { Text(profile) },
                                    onClick = { onProfileChange(profile); showProfileMenu = false }
                                )
                            }
                            DropdownMenuItem(text = { Text("+ New Profile") }, onClick = { onNavigate("settings"); showProfileMenu = false })
                        }
                    }
                }
                if (session?.role == "admin") {
                    Button(onClick = { onNavigate("admin") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))) {
                        Text("Admin")
                    }
                }
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Balance Card
            GlassCard {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Current Balance", color = Color.Gray)
                    Text("${envelope?.balance ?: 0} coins", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                    LinearProgressIndicator(
                        progress = { (envelope?.progress ?: 0) / 100f },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                    Text("Goal: ${envelope?.goal ?: 0} • ${envelope?.progress ?: 0}%", fontSize = 12.sp)
                }
            }

            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Today" to envelope?.dashboardStats?.today, "Week" to envelope?.dashboardStats?.week, "Month" to envelope?.dashboardStats?.month).forEach { (label, value) ->
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text(label, fontSize = 12.sp, color = Color.Gray)
                            Text("${value ?: 0}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quick Actions (Grid Layout)
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            val actions = envelope?.settings?.quickActions ?: emptyList()
            if (actions.isNotEmpty()) {
                // Using a simple Column of Rows to simulate Flow/Grid inside a ScrollView 
                // (LazyVerticalGrid doesn't nest well in Column without fixed height)
                val chunked = actions.chunked(2)
                chunked.forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { action ->
                            Button(
                                onClick = {
                                    if (action.isPositive) onAddIncome(action.value, action.text, null)
                                    else onAddExpense(action.value, action.text, null)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(action.text, color = Color.White)
                                    Text(
                                        if (action.isPositive) "+${action.value}" else "-${action.value}",
                                        fontSize = 12.sp,
                                        color = if (action.isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                        if (rowItems.size < 2) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                Text("No quick actions set.", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Add / Spend Forms
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Add Coins
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("Add Coins", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = addAmount, onValueChange = { addAmount = it },
                        placeholder = { Text("Amt") }, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(Modifier.height(4.dp))
                    // Simplified dropdown for source (using text field for brevity in demo, ideally a dropdown)
                    OutlinedTextField(value = addSource, onValueChange = { addSource = it }, label = { Text("Source") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            addAmount.toIntOrNull()?.let { onAddIncome(it, addSource, null); addAmount = "" }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Add") }
                }

                // Spend Coins
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("Spend Coins", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = spendAmount, onValueChange = { spendAmount = it },
                        placeholder = { Text("Amt") }, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(value = spendCategory, onValueChange = { spendCategory = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            spendAmount.toIntOrNull()?.let { onAddExpense(it, spendCategory, null); spendAmount = "" }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Spend") }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            // Footer
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = { /* Open link */ }) {
                    Text("Buy me a cha ☕ • By Ifti", color = Color.White.copy(alpha = 0.7f))
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}