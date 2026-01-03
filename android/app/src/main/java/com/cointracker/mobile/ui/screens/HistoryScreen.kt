package com.cointracker.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight // Fix for 'FontWeight'
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Fix for 'sp'
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
    var searchQuery by remember { mutableStateOf("") }
    var filterSource by remember { mutableStateOf("All") }
    var currentPage by remember { mutableIntStateOf(0) }
    val itemsPerPage = 10

    // Filter Logic
    val allTransactions = envelope?.transactions ?: emptyList()
    val filteredList = allTransactions.filter {
        (filterSource == "All" || it.source == filterSource) &&
                (searchQuery.isBlank() || it.source.contains(searchQuery, ignoreCase = true) || it.amount.toString().contains(searchQuery))
    }.sortedByDescending { it.date }

    val totalPages = maxOf(1, (filteredList.size + itemsPerPage - 1) / itemsPerPage)
    val currentItems = filteredList.drop(currentPage * itemsPerPage).take(itemsPerPage)

    // Extract unique sources for filter
    val sources = listOf("All") + allTransactions.map { it.source }.distinct().sorted()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("History", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.weight(1f))
            Button(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(12.dp))

        // Filters
        GlassCard {
            Column(modifier = Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; currentPage = 0 },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha=0.1f))) {
                        Text("Filter: $filterSource")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        sources.forEach { src ->
                            DropdownMenuItem(text = { Text(src) }, onClick = { filterSource = src; currentPage = 0; expanded = false })
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // List
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(currentItems) { tx ->
                GlassCard {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.source, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(tx.date.take(10), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                if (tx.amount >= 0) "+${tx.amount}" else "${tx.amount}",
                                color = if (tx.amount >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Row {
                                TextButton(onClick = { onEdit(tx.id, tx.amount, tx.source, tx.date) }) { Text("Edit", fontSize = 12.sp) }
                                TextButton(onClick = { onDelete(tx.id) }) { Text("Del", color = Color(0xFFEF4444), fontSize = 12.sp) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // Pagination
        if (totalPages > 1) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (currentPage > 0) currentPage-- }, enabled = currentPage > 0) {
                    Icon(Icons.Default.ArrowBack, "Prev", tint = Color.White)
                }
                Text("Page ${currentPage + 1} of $totalPages", color = Color.White)
                IconButton(onClick = { if (currentPage < totalPages - 1) currentPage++ }, enabled = currentPage < totalPages - 1) {
                    Icon(Icons.Default.ArrowForward, "Next", tint = Color.White)
                }
            }
        }
    }
}