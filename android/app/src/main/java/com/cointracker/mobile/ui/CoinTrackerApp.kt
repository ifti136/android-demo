package com.cointracker.mobile.ui

import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import com.cointracker.mobile.data.QuickAction
import com.cointracker.mobile.data.Settings
import com.cointracker.mobile.data.Transaction
import com.cointracker.mobile.data.defaultSettings
import com.cointracker.mobile.ui.screens.AdminScreen // Ensure you have this file
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.cointracker.mobile.ui.screens.*
import com.cointracker.mobile.ui.theme.CoinTrackerTheme

// Web Gradient Colors
val Gradient1 = Color(0xFFC3AED6)
val Gradient2 = Color(0xFFF0ABFC)
val Gradient3 = Color(0xFFA1C4FD)
val Gradient4 = Color(0xFFFDF8C8)
// ----------------------------------------------------------------
// THEME COLORS (Matched to Web style.css)
// ----------------------------------------------------------------
val WebPrimary = Color(0xFF3B82F6) // --primary-color
val WebSuccess = Color(0xFF10B981) // --success-color
val WebDanger = Color(0xFFEF4444)  // --danger-color
val WebText = Color(0xFF1E293B)
val WebMuted = Color(0xFF64748B)

@Composable
fun AnimatedGradientBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    // Animate colors to simulate the CSS gradient movement
    val c1 by infiniteTransition.animateColor(
        initialValue = Gradient1, targetValue = Gradient2,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse), label = "c1"
    )
    val c2 by infiniteTransition.animateColor(
        initialValue = Gradient3, targetValue = Gradient4,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse), label = "c2"
    )

    Box(
        modifier = modifier
            .background(Brush.linearGradient(listOf(c1, c2)))
    ) {
        content()
    }
}

// Web Gradient: linear-gradient(125deg, #c3aed6, #f0abfc, #a1c4fd, #fdf8c8)
// Adapted for mobile (Darker glass feel)
val DarkGlassGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1E1B3A), Color(0xFF4A0F4B), Color(0xFF0B3A5D))
)

@Composable
fun GlassTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = WebPrimary,
            secondary = WebSuccess,
            error = WebDanger,
            background = Color.Transparent,
            surface = Color.White.copy(alpha = 0.1f),
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
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}

// ----------------------------------------------------------------
// 1. MAIN NAVIGATION HOST
// ----------------------------------------------------------------
@Composable
fun CoinTrackerApp(viewModel: CoinTrackerViewModel = viewModel()) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    CoinTrackerTheme {
        AnimatedGradientBackground(modifier = Modifier.fillMaxSize()) {
            if (uiState.session == null) {
                LoginNavigation(viewModel)
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.statusBars,
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF1A1D23).copy(alpha = 0.9f),
                            contentColor = Color(0xFF3B82F6)
                        ) {
                            val currentBackStack by navController.currentBackStackEntryAsState()
                            val currentRoute = currentBackStack?.destination?.route ?: "dashboard"
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
                                    onClick = {
                                        if (currentRoute != route) {
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF3B82F6),
                                        indicatorColor = Color(0xFF3B82F6).copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "dashboard") {
                            composable("dashboard") {
                                DashboardScreen(
                                    envelope = uiState.profileEnvelope,
                                    session = uiState.session,
                                    loading = uiState.loading,
                                    profiles = uiState.profiles,
                                    onAddIncome = { amt, src, date -> viewModel.addTransaction(amt, src, date) },
                                    onAddExpense = { amt, src, date -> viewModel.addTransaction(-amt, src, date) },
                                    onProfileChange = { viewModel.switchProfile(it) },
                                    onNavigate = { route -> navController.navigate(route) },
                                    onLogout = { viewModel.logout() }
                                )
                            }
                            composable("analytics") {
                                AnalyticsScreen(
                                    envelope = uiState.profileEnvelope,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("history") {
                                HistoryScreen(
                                    envelope = uiState.profileEnvelope,
                                    onDelete = { viewModel.deleteTransaction(it) },
                                    onEdit = { id, amt, src, date -> viewModel.updateTransaction(id, amt, src, date) },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    envelope = uiState.profileEnvelope,
                                    profiles = uiState.profiles,
                                    onUpdateGoal = { viewModel.updateSettings((uiState.profileEnvelope?.settings ?: defaultSettings()).copy(goal = it)) },
                                    onAddQuickAction = { viewModel.addQuickAction(it) },
                                    onDeleteQuickAction = { viewModel.deleteQuickAction(it) },
                                    onCreateProfile = { viewModel.createProfile(it) },
                                    onImportData = { json ->
                                        Toast.makeText(context, "Import not available in demo", Toast.LENGTH_SHORT).show()
                                    },
                                    onExportData = {
                                        // Manual string export to avoid missing Gson dependency
                                        val data = uiState.profileEnvelope?.transactions.toString()
                                        clipboardManager.setText(AnnotatedString(data))
                                        Toast.makeText(context, "Data copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("admin") {
                                LaunchedEffect(Unit) { viewModel.loadAdmin() }
                                AdminScreen(
                                    session = uiState.session,
                                    stats = uiState.adminStats,
                                    users = uiState.adminUsers,
                                    loading = uiState.loading,
                                    onRefresh = { viewModel.loadAdmin() },
                                    onDeleteUser = { viewModel.deleteUser(it) },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
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
    val uiState by viewModel.uiState.collectAsState()
    val balance = uiState.profileEnvelope?.balance ?: 0
    val settings = uiState.profileEnvelope?.settings ?: defaultSettings()
    val loading = uiState.loading
    val stats = uiState.profileEnvelope?.dashboardStats

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Coin Tracker", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            // Fix #6: Counters above Balance
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCard("Today", "${stats?.today ?: 0}", Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                StatCard("Week", "${stats?.week ?: 0}", Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                StatCard("Month", "${stats?.month ?: 0}", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Balance Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Total Balance", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text("$ $balance", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = WebPrimary)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Fix #5: Progress Bar
                    val progress = if (settings.goal > 0) balance.toFloat() / settings.goal.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = WebSuccess,
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Goal: $ ${settings.goal} (${(progress * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            // Fix #7: Vertical List (Serial Order)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                settings.quickActions.forEach { action ->
                    GlassCard(
                        onClick = {
                            viewModel.addTransaction(
                                if (action.isPositive) action.value else -action.value,
                                action.text,
                                null
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(action.text, color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (action.isPositive) "+$${action.value}" else "-$${action.value}",
                                color = if (action.isPositive) WebSuccess else WebDanger,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = WebPrimary)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ----------------------------------------------------------------
// 3. ANALYTICS SCREEN
// ----------------------------------------------------------------
@Composable
fun AnalyticsScreen(viewModel: CoinTrackerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions = uiState.profileEnvelope?.transactions ?: emptyList()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Analytics", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))

        // Fix #8: Graph Display
        GlassCard(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(4.dp)) {
            if (transactions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data to display", color = Color.Gray) }
            } else {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)) {
                    val points = transactions.runningFold(0f) { sum, tx -> sum + tx.amount.toFloat() }
                    if (points.isNotEmpty()) {
                        val max = points.maxOrNull() ?: 1f
                        val min = points.minOrNull() ?: 0f
                        val range = (max - min).coerceAtLeast(1f)
                        val widthPerPoint = size.width / (points.size - 1).coerceAtLeast(1)

                        val path = Path()
                        points.forEachIndexed { i, balance ->
                            val x = i * widthPerPoint
                            val y = size.height - ((balance - min) / range * size.height)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path = path, color = WebPrimary, style = Stroke(width = 4.dp.toPx()))
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
    val uiState by viewModel.uiState.collectAsState()
    val transactions = uiState.profileEnvelope?.transactions ?: emptyList()
    var selectedTx by remember { mutableStateOf<Transaction?>(null) }

    // Fix #9: Transaction Details & Delete option
    if (selectedTx != null) {
        AlertDialog(
            onDismissRequest = { selectedTx = null },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Transaction Details", color = Color.White) },
            text = {
                Column {
                    Text("Source: ${selectedTx?.source}", color = Color.White)
                    Text(
                        "Amount: ${selectedTx?.amount}",
                        color = if ((selectedTx?.amount ?: 0) >= 0) WebSuccess else WebDanger
                    )
                    Text("Date: ${selectedTx?.date?.take(10)}", color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(selectedTx!!.id)
                    selectedTx = null
                }) { Text("Delete", color = WebDanger) }
            },
            dismissButton = {
                TextButton(onClick = { selectedTx = null }) { Text("Close", color = Color.Gray) }
            }
        )
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("History", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        // Fix #9: LazyColumn for Scrolling ("Pagination")
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(transactions.reversed()) { tx ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { selectedTx = tx }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tx.source, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(tx.date.take(10), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(
                            text = if (tx.amount >= 0) "+${tx.amount}" else "${tx.amount}",
                            color = if (tx.amount >= 0) WebSuccess else WebDanger,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------
// 5. SETTINGS SCREEN
// ----------------------------------------------------------------
@Composable
fun SettingsScreen(viewModel: CoinTrackerViewModel, navController: androidx.navigation.NavController) {
    val uiState by viewModel.uiState.collectAsState()
    val session = uiState.session
    val settings = uiState.profileEnvelope?.settings ?: defaultSettings()
    val context = LocalContext.current

    var showProfileDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf(settings.goal.toString()) }

    // Quick Action State
    var actionText by remember { mutableStateOf("") }
    var actionValue by remember { mutableStateOf("") }
    var actionIsPositive by remember { mutableStateOf(true) }

    // Fix #3: Profile Dialog
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Switch Profile", color = Color.White) },
            text = {
                LazyColumn {
                    items(uiState.profiles) { profile ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchProfile(profile)
                                    showProfileDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = WebPrimary)
                            Spacer(Modifier.width(12.dp))
                            Text(profile, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showProfileDialog = false }) { Text("Cancel", color = Color.Gray) } }
        )
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())) {

        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        // --- Profile Section ---
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("User Profile", style = MaterialTheme.typography.titleMedium, color = WebPrimary)
                Divider(color = Color.White.copy(alpha=0.1f), modifier = Modifier.padding(vertical = 8.dp))
                Text("Username: ${session?.username}", color = Color.White)
                Text("Role: ${session?.role}", color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        GlassCard(onClick = { showProfileDialog = true }) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = WebPrimary)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Switch Profile", color = Color.White)
            }
        }

        // --- Fix #10: Goal Setting ---
        Spacer(modifier = Modifier.height(16.dp))
        Text("Goal Setting", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        GlassCard {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it },
                    label = { Text("Target Amount", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = WebPrimary, unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val newGoal = goalInput.toIntOrNull()
                        if (newGoal != null) {
                            viewModel.updateSettings(settings.copy(goal = newGoal))
                            Toast.makeText(context, "Goal Updated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WebPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Update Goal") }
            }
        }

        // --- Fix #10: Manage Quick Actions ---
        Spacer(modifier = Modifier.height(16.dp))
        Text("Manage Quick Actions", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        GlassCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add New Action", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = actionText, onValueChange = { actionText = it },
                    label = { Text("Label", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = WebPrimary, unfocusedBorderColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = actionValue, onValueChange = { actionValue = it },
                        label = { Text("Amount", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = WebPrimary, unfocusedBorderColor = Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { actionIsPositive = !actionIsPositive },
                        colors = ButtonDefaults.buttonColors(containerColor = if (actionIsPositive) WebSuccess else WebDanger),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) { Text(if (actionIsPositive) "+" else "-") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val valInt = actionValue.toIntOrNull()
                        if (actionText.isNotEmpty() && valInt != null) {
                            viewModel.addQuickAction(QuickAction(actionText, valInt, actionIsPositive))
                            actionText = ""; actionValue = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WebPrimary)
                ) { Text("Add Action") }

                Divider(color = Color.White.copy(alpha=0.1f), modifier = Modifier.padding(vertical = 12.dp))

                // List existing actions
                settings.quickActions.forEachIndexed { index, action ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${action.text} (${if(action.isPositive)+action.value else -action.value})", color = Color.White, fontSize = 14.sp)
                        IconButton(onClick = { viewModel.deleteQuickAction(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WebDanger)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Admin & Logout ---
        if (session?.role.equals("admin", ignoreCase = true)) {
            GlassCard(onClick = {
                viewModel.loadAdmin()
                navController.navigate("admin")
            }) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = WebDanger)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Admin Panel", color = Color.White)
                }
            }
        } else {
            GlassCard(onClick = { Toast.makeText(context, "Insufficient Permissions", Toast.LENGTH_SHORT).show() }) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Admin Panel (Locked)", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, WebDanger),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WebDanger)
        ) { Text("Logout") }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ----------------------------------------------------------------
// 6. LOGIN / REGISTER SCREEN
// ----------------------------------------------------------------
@Composable
fun LoginNavigation(viewModel: CoinTrackerViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

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
                        focusedBorderColor = WebPrimary, unfocusedBorderColor = Color.Gray
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
                        focusedBorderColor = WebPrimary, unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.error ?: "", color = WebDanger, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { if (isRegisterMode) viewModel.register(username, password) else viewModel.login(username, password) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WebPrimary),
                    enabled = !uiState.loading
                ) {
                    if (uiState.loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text(if (isRegisterMode) "Register" else "Login")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fix #13: Toggle Button
                TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                    Text(
                        text = if (isRegisterMode) "Already have an account? Login" else "Don't have an account? Register",
                        color = WebPrimary
                    )
                }
            }
        }
    }
}