package com.example.sleepydriverapp.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Vibrator
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepydriverapp.data.model.AppSettings
import com.example.sleepydriverapp.data.model.DetectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlin.math.pow
import kotlin.math.abs

class SleepyDriverViewModel : ViewModel() {
    val appSettings = mutableStateOf(AppSettings())
    val detectionState = mutableStateOf(DetectionState())
    val isDetectionEnabled = mutableStateOf(false)
    val showHelpDialog = mutableStateOf(false)
    val showSettingsDialog = mutableStateOf(false)
    val hasCameraPermission = mutableStateOf(false)
    val showCamera = mutableStateOf(false)
    val cameraInitialized = mutableStateOf(false)
    val isToggling = mutableStateOf(false)

    // Toggle animation progress (0.0 to 1.0)
    val toggleProgress = mutableStateOf(0f)

    private var toggleJob: Job? = null
    private var blinkResetJob: Job? = null
    private var lastAlertTime = 0L // Theo dõi thời gian cảnh báo cuối cùng để tránh spam

    fun checkCameraPermission(context: Context) {
        hasCameraPermission.value = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun handleToggleDetection(context: Context, requestPermission: () -> Unit) {
        if (isToggling.value) return

        toggleJob?.cancel()
        isToggling.value = true

        val willTurnOn = !isDetectionEnabled.value // Lưu trạng thái sẽ chuyển đến

        if (willTurnOn) {
            // Turning ON
            if (hasCameraPermission.value) {
                // Cập nhật trạng thái NGAY khi bắt đầu
                isDetectionEnabled.value = true

                toggleJob = viewModelScope.launch {
                    // Animate toggle to ON position
                    animateToggle(true)

                    // Enable UI components after animation
                    showCamera.value = true

                    // Wait for camera initialization
                    delay(200L)
                    cameraInitialized.value = true
                    isToggling.value = false

                    android.widget.Toast.makeText(
                        context,
                        "✅ Bắt đầu giám sát ngủ gật",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Reset toggle position if no permission
                isToggling.value = false
                requestPermission()
            }
        } else {
            // Turning OFF
            // Cập nhật trạng thái NGAY khi bắt đầu
            isDetectionEnabled.value = false

            toggleJob = viewModelScope.launch {
                // Animate toggle to OFF position
                animateToggle(false)

                // Disable components after animation
                showCamera.value = false
                cameraInitialized.value = false
                detectionState.value = DetectionState()
                isToggling.value = false

                android.widget.Toast.makeText(
                    context,
                    "⏸️ Dừng giám sát ngủ gật",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun animateToggle(toOn: Boolean) {
        val startProgress = toggleProgress.value
        val targetProgress = if (toOn) 1f else 0f
        val duration = 400L
        val steps = 48
        val stepDuration = duration / steps

        repeat(steps) { step ->
            val progress = step.toFloat() / (steps - 1)
            val easedProgress = easeInOutCubic(progress)
            toggleProgress.value = startProgress + (targetProgress - startProgress) * easedProgress
            delay(stepDuration)
        }

        toggleProgress.value = targetProgress
    }

    private fun easeInOutCubic(t: Float): Float {
        return if (t < 0.5f) {
            4 * t * t * t
        } else {
            1 - (-2 * t + 2).pow(3.0f) / 2
        }
    }

    fun onPermissionResult(isGranted: Boolean, context: Context) {
        hasCameraPermission.value = isGranted
        if (isGranted) {
            // Chỉ bật khi user thực sự muốn bật (không tự động bật lại)
            if (isDetectionEnabled.value) {
                viewModelScope.launch {
                    showCamera.value = true
                    delay(200L)
                    cameraInitialized.value = true
                    isToggling.value = false

                    android.widget.Toast.makeText(
                        context,
                        "✅ Bắt đầu giám sát ngủ gật",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            viewModelScope.launch {
                // Reset states when permission denied
                isDetectionEnabled.value = false
                showCamera.value = false
                cameraInitialized.value = false
                toggleProgress.value = 0f
                isToggling.value = false

                android.widget.Toast.makeText(
                    context,
                    "Cần quyền camera để hoạt động",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun updateDetectionState(newState: DetectionState, context: Context) {
        val currentState = detectionState.value

        // Only update if there's a significant change
        if (shouldUpdateState(currentState, newState)) {
            detectionState.value = newState

            // Kích hoạt thông báo khi phát hiện nhắm mắt
            if (newState.eyesClosed && newState.eyesClosedDuration >= appSettings.value.alertThreshold) {
                val currentTime = System.currentTimeMillis()
                // Chỉ hiển thị thông báo nếu đã qua 2 giây kể từ thông báo trước
                if (currentTime - lastAlertTime >= 2000L) {
                    lastAlertTime = currentTime
                    viewModelScope.launch {
                        // Hiển thị Toast
                        android.widget.Toast.makeText(
                            context,
                            "🚨 CẢNH BÁO: Nhắm mắt quá lâu!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        // Phát âm thanh nếu được bật
                        if (appSettings.value.soundEnabled) {
                            try {
                                val mediaPlayer = MediaPlayer.create(context, appSettings.value.soundResourceId)
                                mediaPlayer?.setVolume(appSettings.value.soundVolume, appSettings.value.soundVolume)
                                mediaPlayer?.start()
                                mediaPlayer?.setOnCompletionListener { it.release() }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Lỗi phát âm thanh",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        // Rung nếu được bật
                        if (appSettings.value.vibrationEnabled) {
                            try {
                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1) // Rung 2 lần, mỗi lần 500ms
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Lỗi rung",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

            // Reset blink detection after a period of normal state
            if (newState.eyesOpen && !newState.eyesClosed) {
                resetBlinkDetection()
            }
        }
    }

    private fun shouldUpdateState(current: DetectionState, new: DetectionState): Boolean {
        // Always update if face detection status changes
        if (current.faceDetected != new.faceDetected) return true

        // For eye state changes, update immediately for eyesClosed to avoid missing alerts
        if (current.eyesOpen != new.eyesOpen || current.eyesClosed != new.eyesClosed) {
            return true // Bỏ điều kiện eyesClosedDuration > 100L để không bỏ sót nhắm mắt ngắn
        }

        // Always update duration and confidence
        return current.eyesClosedDuration != new.eyesClosedDuration ||
                abs(current.confidenceScore - new.confidenceScore) > 0.05f
    }

    private fun resetBlinkDetection() {
        blinkResetJob?.cancel()
        blinkResetJob = viewModelScope.launch {
            delay(2000L) // Wait 2 seconds of normal state
            if (detectionState.value.eyesOpen && !detectionState.value.eyesClosed) {
                // Reset any accumulated closed duration
                detectionState.value = detectionState.value.copy(eyesClosedDuration = 0L)
            }
        }
    }

    fun resetSettings(context: Context) {
        appSettings.value = AppSettings()
        android.widget.Toast.makeText(
            context,
            "Đã reset về cài đặt mặc định",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    fun handleAutoStart() {
        if (appSettings.value.autoStart &&
            !isDetectionEnabled.value &&
            hasCameraPermission.value &&
            !isToggling.value) {

            viewModelScope.launch {
                isToggling.value = true

                // Animate toggle to ON position
                animateToggle(true)

                // Enable detection
                isDetectionEnabled.value = true
                showCamera.value = true

                delay(200L)
                cameraInitialized.value = true
                isToggling.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        toggleJob?.cancel()
        blinkResetJob?.cancel()
    }
}