package com.cointracker.mobile.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cointracker.mobile.data.ProfileEnvelope
import com.cointracker.mobile.ui.navigation.Destinations
import com.cointracker.mobile.ui.screens.AdminScreen
import com.cointracker.mobile.ui.screens.AnalyticsScreen
import com.cointracker.mobile.ui.screens.DashboardScreen
import com.cointracker.mobile.ui.screens.HistoryScreen
import com.cointracker.mobile.ui.screens.LoginScreen
import com.cointracker.mobile.ui.screens.SettingsScreen
import com.google.firebase.FirebaseApp
import com.cointracker.mobile.ui.collectAsStateWithLifecycleSafe

@SuppressLint("MissingPermission")
@Composable
fun CoinTrackerApp(context: Context? = null) {
    // Ensure Firebase is initialized once.
    LaunchedEffect(Unit) {
        val appContext = context ?: LocalContext.current
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext)
        }
    }

    val navController = rememberNavController()
    val snackbarHostState: SnackbarHostState = rememberSnackbarHostState()
    val viewModel: CoinTrackerViewModel = viewModel(factory = CoinTrackerViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycleSafe()

    var lastError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
        lastError = uiState.error
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF111827),
                        Color(0xFF1E293B)
                    )
                )
            )
    ) {
        NavHost(
            navController = navController,
            startDestination = if (uiState.session == null) Destinations.Login.route else Destinations.Dashboard.route
        ) {
            composable(Destinations.Login.route) {
                LoginScreen(
                    loading = uiState.loading,
                    onLogin = { u, p -> viewModel.login(u, p) },
                    onRegister = { u, p -> viewModel.register(u, p) },
                    loggedIn = uiState.session != null,
                    onSuccess = {
                        navController.navigate(Destinations.Dashboard.route) {
                            popUpTo(Destinations.Login.route) { inclusive = true }
                        }
                    },
                    error = uiState.error
                )
            }
            composable(Destinations.Dashboard.route) {
                val envelope = uiState.profileEnvelope
                DashboardScreen(
                    envelope = envelope,
                    onAddIncome = { amount, source, date -> viewModel.addTransaction(amount, source, date) },
                    onAddExpense = { amount, source, date -> viewModel.addTransaction(-amount, source, date) },
                    onOpenAnalytics = { navController.navigate(Destinations.Analytics.route) },
                    onOpenHistory = { navController.navigate(Destinations.History.route) },
                    onOpenSettings = { navController.navigate(Destinations.Settings.route) },
                    onOpenAdmin = { navController.navigate(Destinations.Admin.route) },
                    session = uiState.session,
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(Destinations.Login.route) { popUpTo(0) }
                    },
                    profiles = uiState.profiles,
                    onProfileChange = { viewModel.switchProfile(it) },
                    loading = uiState.loading
                )
            }
            composable(Destinations.Analytics.route) {
                AnalyticsScreen(envelope = uiState.profileEnvelope) {
                    navController.popBackStack()
                }
            }
            composable(Destinations.History.route) {
                HistoryScreen(
                    envelope = uiState.profileEnvelope,
                    onDelete = { id -> viewModel.deleteTransaction(id) },
                    onEdit = { id, amount, source, date -> viewModel.updateTransaction(id, amount, source, date) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Destinations.Settings.route) {
                SettingsScreen(
                    envelope = uiState.profileEnvelope,
                    profiles = uiState.profiles,
                    onUpdateGoal = { goal ->
                        val settings = uiState.profileEnvelope?.settings?.copy(goal = goal) ?: return@SettingsScreen
                        viewModel.updateSettings(settings)
                    },
                    onAddQuickAction = { action -> viewModel.addQuickAction(action) },
                    onDeleteQuickAction = { index -> viewModel.deleteQuickAction(index) },
                    onCreateProfile = { name -> viewModel.createProfile(name) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Destinations.Admin.route) {
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

        SnackbarHost(hostState = snackbarHostState)
    }
}
