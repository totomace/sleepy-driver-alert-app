package com.example.sleepydriverapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.*
import com.example.sleepydriverapp.ui.screen.MainScreen
import com.example.sleepydriverapp.ui.screen.MonitorScreen
import com.example.sleepydriverapp.ui.theme.SleepyDriverAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepyDriverAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Trạng thái theo dõi giám sát
    var isMonitoring by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                isMonitoring = isMonitoring,
                onToggleMonitoring = { value ->
                    isMonitoring = value
                    if (value) {
                        navController.navigate("monitor")
                    }
                },
                onShowInstructions = {
                    // TODO: Chuyển đến màn hướng dẫn nếu cần
                },
                onShowSettings = {
                    // TODO: Chuyển đến màn cài đặt nếu cần
                }
            )
        }

        composable("monitor") {
            MonitorScreen()
        }
    }
}
