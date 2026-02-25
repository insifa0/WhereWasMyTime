package com.example.wherewasmytime.ui.screens.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wherewasmytime.WhereWasMyTimeApp
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class GoalProgress(
    val category: CategoryEntity,
    val todayMinutes: Int,
    val goalMinutes: Int,          // 0 = hedef yok
    val percentage: Float          // 0f – 1f+
)

data class GoalsUiState(
    val goals: List<GoalProgress> = emptyList(),
    val editingCategory: CategoryEntity? = null   // Sheet açıkken
)

class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as WhereWasMyTimeApp).repository

    private val _editing = MutableStateFlow<CategoryEntity?>(null)

    val uiState: StateFlow<GoalsUiState> = combine(
        repository.getActiveCategories(),
        repository.getTodaySessions(),
        _editing
    ) { categories, todaySessions, editing ->
        val goals = categories.map { cat ->
            val todayMins = todaySessions
                .filter { it.categoryId == cat.id }
                .sumOf { it.durationMinutes }
            val goal = cat.dailyGoalMinutes ?: 0
            GoalProgress(
                category = cat,
                todayMinutes = todayMins,
                goalMinutes = goal,
                percentage = if (goal > 0) todayMins.toFloat() / goal else 0f
            )
        }.sortedByDescending { it.goalMinutes }

        GoalsUiState(goals = goals, editingCategory = editing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsUiState()
    )

    fun startEditing(category: CategoryEntity) {
        _editing.value = category
    }

    fun closeEditing() {
        _editing.value = null
    }

    fun saveGoal(category: CategoryEntity, newGoalMinutes: Int) {
        viewModelScope.launch {
            repository.updateCategory(category.copy(dailyGoalMinutes = newGoalMinutes.takeIf { it > 0 }))
            _editing.value = null
        }
    }
}
