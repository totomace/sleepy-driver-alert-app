package com.example.sleepydriverapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Header(
    onShowInstructions: () -> Unit,
    onShowSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "SLEEPY DRIVER",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Giám sát buồn ngủ khi lái xe",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                fontSize = 14.sp
            )
        }

        Row {
            IconButton(onClick = onShowInstructions) {
                Icon(Icons.Default.Help, contentDescription = "Hướng dẫn")
            }
            IconButton(onClick = onShowSettings) {
                Icon(Icons.Default.Menu, contentDescription = "Cài đặt")
            }
        }
    }
}
