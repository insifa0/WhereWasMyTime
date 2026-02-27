package com.example.wherewasmytime.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wherewasmytime.WhereWasMyTimeApp
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import com.example.wherewasmytime.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val activeCategories: List<CategoryEntity> = emptyList(),
    val todaySessions: List<SessionEntity> = emptyList(),
    val recentSessions: List<SessionEntity> = emptyList(),  // Son Aktiviteler listesi (tarihsiz)
    val totalTodayMinutes: Int = 0,
    val activeSession: SessionEntity? = null,
    val categoriesMap: Map<Long, CategoryEntity> = emptyMap()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as WhereWasMyTimeApp).repository

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getActiveCategories(),
        repository.getAllCategories(),
        repository.getTodaySessions(),
        repository.getActiveSession(),
        repository.getRecentSessions(limit = 10)
    ) { activeCategories, allCategories, todaySessions, activeSession, recentSessions ->
        HomeUiState(
            activeCategories = activeCategories,
            todaySessions = todaySessions,
            recentSessions = recentSessions,
            totalTodayMinutes = todaySessions.sumOf { it.durationMinutes },
            activeSession = activeSession,
            categoriesMap = allCategories.associateBy { it.id }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun archiveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.archiveCategory(category.id)
        }
    }

    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session.id)
        }
    }

    fun insertSession(session: SessionEntity) {
        viewModelScope.launch {
            repository.insertSession(session)
        }
    }

    fun updateSession(session: SessionEntity) {
        viewModelScope.launch {
            repository.updateSession(session)
        }
    }
}
