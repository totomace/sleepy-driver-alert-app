package com.example.sleepydriverapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sleepydriverapp.ui.screen.MainScreen
import com.example.sleepydriverapp.ui.screen.MonitorScreen
import com.example.sleepydriverapp.ui.theme.SleepyDriverAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepyDriverAppTheme {
                val navController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            MainScreen(
                                onStartMonitoring = {
                                    navController.navigate("monitor")
                                },
                                onShowInstructions = { /* TODO */ },
                                onSimulationMode = { /* TODO */ }
                            )
                        }
                        composable("monitor") {
                            MonitorScreen()
                        }
                    }
                }
            }
        }
    }
}
