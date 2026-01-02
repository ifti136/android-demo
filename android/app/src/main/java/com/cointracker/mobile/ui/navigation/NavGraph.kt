package com.cointracker.mobile.ui.navigation

sealed class Destinations(val route: String) {
    data object Login : Destinations("login")
    data object Dashboard : Destinations("dashboard")
    data object Analytics : Destinations("analytics")
    data object History : Destinations("history")
    data object Settings : Destinations("settings")
    data object Admin : Destinations("admin")
}
