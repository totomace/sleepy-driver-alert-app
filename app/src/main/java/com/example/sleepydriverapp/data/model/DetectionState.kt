package com.example.sleepydriverapp.data.model

data class DetectionState(
    val faceDetected: Boolean = false,
    val eyesOpen: Boolean = true,
    val eyesClosed: Boolean = false,
    val eyesClosedDuration: Long = 0L,
    val confidenceScore: Float = 0f,
    val eyesClosedStartTime: Long = 0L
)