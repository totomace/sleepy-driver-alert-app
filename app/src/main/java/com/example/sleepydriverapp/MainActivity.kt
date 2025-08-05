package com.example.sleepydriverapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sleepydriverapp.ui.theme.SleepyDriverAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepyDriverAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sleepy Driver App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(onClick = { /* TODO: mở camera */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Bắt đầu giám sát")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* TODO: hiện hướng dẫn */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Hướng dẫn sử dụng")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* TODO: mô phỏng */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Chế độ mô phỏng")
        }
    }
}
