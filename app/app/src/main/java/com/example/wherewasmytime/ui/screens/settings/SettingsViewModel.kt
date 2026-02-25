package com.example.wherewasmytime.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wherewasmytime.WhereWasMyTimeApp
import com.example.wherewasmytime.data.preferences.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as WhereWasMyTimeApp
    private val prefs = app.userPreferences
    private val repository = app.repository

    val currentTheme: StateFlow<AppTheme> = prefs.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.DARK)

    fun setTheme(theme: AppTheme) = prefs.setTheme(theme)

    fun wipeAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllSessions()
            onDone()
        }
    }
}
