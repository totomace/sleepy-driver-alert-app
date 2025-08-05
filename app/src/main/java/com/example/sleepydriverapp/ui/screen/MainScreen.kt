package com.example.sleepydriverapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    onStartMonitoring: () -> Unit,
    onShowInstructions: () -> Unit,
    onSimulationMode: () -> Unit
) {
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

        Button(
            onClick = onStartMonitoring,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Bắt đầu giám sát")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onShowInstructions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hướng dẫn sử dụng")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSimulationMode,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Chế độ mô phỏng")
        }
    }
}
