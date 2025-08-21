package com.example.sleepydriverapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingWithToggle(
    icon: String,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    nightMode: Boolean = false,
    isSmallScreen: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
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
            Text(
                text = description,
                fontSize = if (isSmallScreen) 12.sp else 14.sp,
                color = if (nightMode) Color(0xFF999999) else Color(0xFF666666),
                lineHeight = if (isSmallScreen) 14.sp else 16.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFF6B35),
                checkedTrackColor = Color(0xFFFF6B35).copy(alpha = 0.5f)
            )
        )
    }
}