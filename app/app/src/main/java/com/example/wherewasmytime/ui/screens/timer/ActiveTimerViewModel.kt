package com.example.wherewasmytime.ui.screens.timer

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wherewasmytime.WhereWasMyTimeApp
import com.example.wherewasmytime.service.TimerService
import com.example.wherewasmytime.service.TimerState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActiveTimerViewModel(application: Application) : AndroidViewModel(application) {

    // TimerService'deki global StateFlow'u doğrudan dinliyoruz
    val timerState: StateFlow<TimerState> = TimerService.timerState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimerService.timerState.value
        )

    fun startTimer(categoryId: Long, categoryName: String) {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_CATEGORY_ID, categoryId)
            putExtra(TimerService.EXTRA_CATEGORY_NAME, categoryName)
        }
        context.startForegroundService(intent)
    }

    fun pauseTimer() {
        sendAction(TimerService.ACTION_PAUSE)
    }

    fun resumeTimer() {
        sendAction(TimerService.ACTION_RESUME)
    }

    fun stopTimer() {
        sendAction(TimerService.ACTION_STOP)
    }

    private fun sendAction(action: String) {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }

    fun updatePhotoPath(sessionId: Long, photoPath: String) {
        val app = getApplication<WhereWasMyTimeApp>()
        viewModelScope.launch {
            app.repository.updateSessionPhoto(sessionId, photoPath)
        }
    }
}
