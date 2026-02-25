package com.example.wherewasmytime.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.wherewasmytime.MainActivity
import com.example.wherewasmytime.WhereWasMyTimeApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Zamanlayıcının durumunu tutar.
 * Companion object üzerinden hem Service hem ViewModel erişebilir.
 */
data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val sessionId: Long = -1L,
    val categoryId: Long = -1L,
    val categoryName: String = "",
    val startTimeMs: Long = 0L
)

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    companion object {
        // Servis İşlem Komutları
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"

        // Intent Ekstra Anahtarları
        const val EXTRA_CATEGORY_ID = "EXTRA_CATEGORY_ID"
        const val EXTRA_CATEGORY_NAME = "EXTRA_CATEGORY_NAME"

        // Bildirim
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "timer_channel"

        // Tüm uygulamanın gözlemleyebileceği timer durumu
        private val _timerState = MutableStateFlow(TimerState())
        val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

        fun isRunning() = _timerState.value.isRunning || _timerState.value.isPaused
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val categoryId = intent.getLongExtra(EXTRA_CATEGORY_ID, -1L)
                val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: ""
                startTimer(categoryId, categoryName)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    private fun startTimer(categoryId: Long, categoryName: String) {
        val startTimeMs = System.currentTimeMillis()

        serviceScope.launch {
            // Önce DB'ye oturum başlat
            val sessionId = (application as WhereWasMyTimeApp).repository.startSession(categoryId)

            _timerState.value = TimerState(
                isRunning = true,
                isPaused = false,
                elapsedSeconds = 0L,
                sessionId = sessionId,
                categoryId = categoryId,
                categoryName = categoryName,
                startTimeMs = startTimeMs
            )

            createNotificationChannel()
            startForeground(NOTIFICATION_ID, buildNotification(categoryName, 0L))
            startTicking()
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false, isPaused = true)
        updateNotification()
    }

    private fun resumeTimer() {
        _timerState.value = _timerState.value.copy(isRunning = true, isPaused = false)
        startTicking()
    }

    private fun stopTimer() {
        timerJob?.cancel()
        val state = _timerState.value

        serviceScope.launch {
            if (state.sessionId > 0) {
                (application as WhereWasMyTimeApp).repository.finishSession(
                    sessionId = state.sessionId,
                    startTime = state.startTimeMs
                )
            }
        }

        _timerState.value = TimerState() // Sıfırla
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTicking() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                if (_timerState.value.isRunning) {
                    val newElapsed = _timerState.value.elapsedSeconds + 1
                    _timerState.value = _timerState.value.copy(elapsedSeconds = newElapsed)
                    updateNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        val state = _timerState.value
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(state.categoryName, state.elapsedSeconds))
    }

    private fun buildNotification(categoryName: String, elapsedSeconds: Long): Notification {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        val timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TimerService::class.java).apply { action = ACTION_STOP }
        val pendingStop = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("⏱ $categoryName")
            .setContentText(timeStr)
            .setOngoing(true)
            .setContentIntent(pendingOpen)
            .addAction(android.R.drawable.ic_delete, "Durdur", pendingStop)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Zamanlayıcı",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Aktif zamanlayıcı bildirimi"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
