package com.example.sleepydriverapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.example.sleepydriverapp.ui.screen.SleepyDriverScreen
import com.example.sleepydriverapp.ui.theme.SleepyDriverTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepyDriverTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = Color(0xFFF5F5F5)
                ) {
                    SleepyDriverScreen()
                }
            }
        }
    }
}