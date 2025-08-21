package com.example.sleepydriverapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpItem(icon: String, text: String, nightMode: Boolean = false, isSmallScreen: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = icon,
            fontSize = if (isSmallScreen) 16.sp else 18.sp,
            modifier = Modifier.width(if (isSmallScreen) 28.dp else 32.dp)
        )
        Text(
            text = text,
            fontSize = if (isSmallScreen) 14.sp else 16.sp,
            color = if (nightMode) Color(0xFFCCCCCC) else Color(0xFF424242),
            modifier = Modifier.weight(1f),
            lineHeight = if (isSmallScreen) 18.sp else 20.sp
        )
    }
}