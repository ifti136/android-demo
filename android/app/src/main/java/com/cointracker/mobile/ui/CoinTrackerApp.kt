package com.cointracker.mobile.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cointracker.mobile.data.Transaction

// ----------------------------------------------------------------
// THEME COLORS (Cyberpunk / Glassmorphism)
// ----------------------------------------------------------------
val NeonPurple = Color(0xFFBC13FE)
val NeonCyan = Color(0xFF00F0FF)
val GlassBackground = Color(0xFF121212)
val GlassSurface = Color.White.copy(alpha = 0.05f)
val GlassBorder = Color.White.copy(alpha = 0.2f)

@Composable
fun GlassTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NeonPurple,
            secondary = NeonCyan,
            background = GlassBackground,
            surface = Color.Transparent,
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        border = BorderStroke(1.dp, GlassBorder),
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}

// ----------------------------------------------------------------
// 1. MAIN NAVIGATION HOST
// ----------------------------------------------------------------

@Composable
fun CoinTrackerApp(viewModel: CoinTrackerViewModel = viewModel()) {
    GlassTheme {
        val navController = rememberNavController()
        val userSession by viewModel.userSession.collectAsState()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStack?.destination?.route ?: "dashboard"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E))
                    )
                )
        ) {
            if (userSession == null) {
                LoginScreen(viewModel)
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    // Fix: Navigation Bar is separate from content
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.Black.copy(alpha = 0.6f),
                            contentColor = NeonCyan
                        ) {
                            val items = listOf(
                                "dashboard" to Icons.Default.Home,
                                "analytics" to Icons.Default.DateRange,
                                "history" to Icons.Default.List,
                                "settings" to Icons.Default.Settings
                            )
                            items.forEach { (route, icon) ->
                                NavigationBarItem(
                                    icon = { Icon(icon, contentDescription = route) },
                                    label = { Text(route.replaceFirstChar { it.uppercase() }) },
                                    selected = currentRoute == route,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NeonPurple,
                                        selectedTextColor = NeonPurple,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = Color.White.copy(alpha = 0.1f)
                                    ),
                                    onClick = {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // Fix: Content respects the bottom bar padding
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") { DashboardScreen(viewModel) }
                        composable("analytics") { AnalyticsScreen(viewModel) }
                        composable("history") { HistoryScreen(viewModel) }
                        composable("settings") { SettingsScreen(viewModel) }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------
// 2. DASHBOARD SCREEN
// ----------------------------------------------------------------
@Composable
fun DashboardScreen(viewModel: CoinTrackerViewModel) {
    val balance by viewModel.balance.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Coin Tracker", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Total Balance", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text("$ $balance", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = NeonCyan)
                    Spacer(modifier = Modifier.height(16.dp))

                    val progress = if (settings.goal > 0) balance.toFloat() / settings.goal.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = progress.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = NeonPurple,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Goal: $ ${settings.goal} (${(progress * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCard("Daily", "$0", Modifier.weight(1f)) // Placeholder values
                Spacer(modifier = Modifier.width(8.dp))
                StatCard("Weekly", "$0", Modifier.weight(1f)) // Placeholder values
                Spacer(modifier = Modifier.width(8.dp))
                StatCard("Monthly", "$0", Modifier.weight(1f)) // Placeholder values
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            settings.quickActions.forEach { action ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.addTransaction(if (action.isPositive) action.value else -action.value, action.text) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(action.text, color = Color.White)
                        Text(if (action.isPositive) "+$${action.value}" else "-$${action.value}", color = if (action.isPositive) NeonCyan else Color.Red)
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp)) // Extra space for scroll
        }

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = NeonPurple) }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ----------------------------------------------------------------
// 3. ANALYTICS SCREEN
// ----------------------------------------------------------------
@Composable
fun AnalyticsScreen(viewModel: CoinTrackerViewModel) {
    val transactions by viewModel.transactions.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Analytics", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))

        GlassCard(modifier = Modifier.fillMaxWidth().height(250.dp).padding(8.dp)) {
            if (transactions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data", color = Color.Gray) }
            } else {
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val points = transactions.runningFold(0f) { sum, tx -> sum + tx.amount.toFloat() }
                    if (points.isNotEmpty()) {
                        val max = points.maxOrNull() ?: 1f
                        val min = points.minOrNull() ?: 0f
                        val range = max - min
                        val widthPerPoint = size.width / (points.size - 1).coerceAtLeast(1)
                        val path = Path()
                        points.forEachIndexed { i, balance ->
                            val x = i * widthPerPoint
                            val y = size.height - ((balance - min) / range.coerceAtLeast(1f) * size.height)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path = path, color = NeonCyan, style = Stroke(width = 3.dp.toPx()))
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------
// 4. HISTORY SCREEN
// ----------------------------------------------------------------
@Composable
fun HistoryScreen(viewModel: CoinTrackerViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    var selectedTx by remember { mutableStateOf<Transaction?>(null) }

    if (selectedTx != null) {
        AlertDialog(
            onDismissRequest = { selectedTx = null },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Transaction Details", color = Color.White) },
            text = {
                Column {
                    Text("Source: ${selectedTx?.source}", color = Color.White)
                    Text("Amount: ${selectedTx?.amount}", color = Color.White)
                    Text("Date: ${selectedTx?.date}", color = Color.White)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(selectedTx!!.id)
                    selectedTx = null
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { selectedTx = null }) { Text("Close", color = Color.Gray) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("History", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(transactions.reversed()) { tx ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedTx = tx }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tx.source, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(tx.date.take(10), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(
                            text = if (tx.amount >= 0) "+${tx.amount}" else "${tx.amount}",
                            color = if (tx.amount >= 0) NeonCyan else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------
// 5. SETTINGS SCREEN (Fixed Admin & Profile)
// ----------------------------------------------------------------
@Composable
fun SettingsScreen(viewModel: CoinTrackerViewModel) {
    val session by viewModel.userSession.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("User Profile", style = MaterialTheme.typography.titleMedium, color = NeonPurple)
                Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
                Text("Username: ${session?.username}", color = Color.White)
                Text("Role: ${session?.role}", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fix: Switch Profile Button
        GlassCard(modifier = Modifier.fillMaxWidth().clickable {
            // Add switch logic here or show toast
            Toast.makeText(context, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Switch Profile", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fix: Admin Panel (Only visible if admin)
        if (session?.role.equals("admin", ignoreCase = true)) {
            GlassCard(modifier = Modifier.fillMaxWidth().clickable {
                Toast.makeText(context, "Opening Admin Panel...", Toast.LENGTH_SHORT).show()
                // Navigate to admin screen if you have one
            }) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Admin Panel", color = Color.White)
                }
            }
        } else {
            // Show disabled or just hide it. User asked to "see" permission issues
            GlassCard(modifier = Modifier.fillMaxWidth().clickable {
                Toast.makeText(context, "Insufficient Permissions", Toast.LENGTH_SHORT).show()
            }) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Admin Panel (Locked)", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.Red),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text("Logout")
        }
    }
}

// ----------------------------------------------------------------
// 6. LOGIN / REGISTER SCREEN
// ----------------------------------------------------------------
@Composable
fun LoginScreen(viewModel: CoinTrackerViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GlassCard(modifier = Modifier.padding(32.dp).fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isRegisterMode) "Create Account" else "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White, fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Username", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonPurple, unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password", color = Color.Gray) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonPurple, unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { if (isRegisterMode) viewModel.register(username, password) else viewModel.login(username, password) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    enabled = !loading
                ) {
                    if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text(if (isRegisterMode) "Register" else "Login")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                    Text(
                        text = if (isRegisterMode) "Already have an account? Login" else "Don't have an account? Register",
                        color = NeonCyan
                    )
                }
            }
        }
    }
}