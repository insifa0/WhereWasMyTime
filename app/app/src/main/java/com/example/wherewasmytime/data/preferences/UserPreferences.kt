package com.example.wherewasmytime.data.preferences

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme { DARK, LIGHT, SYSTEM }

class UserPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _themeFlow = MutableStateFlow(readTheme())
    val themeFlow: StateFlow<AppTheme> = _themeFlow.asStateFlow()

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _themeFlow.value = theme
    }

    private fun readTheme(): AppTheme {
        val saved = prefs.getString(KEY_THEME, AppTheme.DARK.name) ?: AppTheme.DARK.name
        return runCatching { AppTheme.valueOf(saved) }.getOrDefault(AppTheme.DARK)
    }

    companion object {
        private const val KEY_THEME = "app_theme"
    }
}
