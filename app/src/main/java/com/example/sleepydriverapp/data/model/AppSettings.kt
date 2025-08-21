package com.example.sleepydriverapp.data.model

import com.example.sleepydriverapp.R

data class AppSettings(
    val sensitivity: Float = 0.5f, // 0.0 - 1.0
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.8f, // 0.0 - 1.0
    val nightMode: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val autoStart: Boolean = false,
    val alertThreshold: Long = 800L, // milliseconds - thời gian nhắm mắt tối đa
    val soundResourceId: Int = R.raw.beep_beep_43875 // Mặc định là beep_beep_43875.mp3
)