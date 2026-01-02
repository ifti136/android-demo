package com.cointracker.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.cointracker.mobile.ui.theme.CoinTrackerTheme
import com.cointracker.mobile.ui.CoinTrackerApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val useDarkTheme = isSystemInDarkTheme()
            CoinTrackerTheme(darkTheme = useDarkTheme) {
                CoinTrackerApp()
            }
        }
    }
}
