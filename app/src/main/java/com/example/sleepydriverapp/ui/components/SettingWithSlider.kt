package com.example.sleepydriverapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingWithSlider(
    icon: String,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueText: String,
    nightMode: Boolean = false,
    isSmallScreen: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = if (isSmallScreen) 18.sp else 20.sp,
                modifier = Modifier.width(if (isSmallScreen) 35.dp else 40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    color = if (nightMode) Color.White else Color(0xFF2D2D2D)
                )
            }
            Text(
                text = valueText,
                fontSize = if (isSmallScreen) 12.sp else 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = Color(0xFFFF6B35)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF6B35),
                activeTrackColor = Color(0xFFFF6B35),
                inactiveTrackColor = if (nightMode) Color(0xFF333333) else Color(0xFFE0E0E0)
            ),
            modifier = Modifier.padding(start = if (isSmallScreen) 35.dp else 40.dp)
        )
    }
}