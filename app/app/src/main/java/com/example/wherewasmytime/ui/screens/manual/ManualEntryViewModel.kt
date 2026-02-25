package com.example.wherewasmytime.ui.screens.manual

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wherewasmytime.WhereWasMyTimeApp
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import com.example.wherewasmytime.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class ManualEntryFormState(
    val selectedCategory: CategoryEntity? = null,
    val selectedDateMs: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val durationMinutes: Int = 30,
    val note: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class ManualEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as WhereWasMyTimeApp).repository

    val categories: StateFlow<List<CategoryEntity>> = repository.getActiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(ManualEntryFormState())
    val form: StateFlow<ManualEntryFormState> = _form.asStateFlow()

    fun setCategory(category: CategoryEntity) {
        _form.value = _form.value.copy(selectedCategory = category, error = null)
    }

    fun setDate(dateMs: Long) {
        _form.value = _form.value.copy(selectedDateMs = dateMs)
    }

    fun setStartTime(hour: Int, minute: Int) {
        _form.value = _form.value.copy(startHour = hour, startMinute = minute)
    }

    fun setDuration(minutes: Int) {
        _form.value = _form.value.copy(durationMinutes = minutes.coerceIn(1, 1440))
    }

    fun setNote(note: String) {
        _form.value = _form.value.copy(note = note)
    }

    fun saveEntry(onSuccess: () -> Unit) {
        val form = _form.value
        if (form.selectedCategory == null) {
            _form.value = form.copy(error = "Lütfen bir kategori seçin")
            return
        }
        if (form.durationMinutes < 1) {
            _form.value = form.copy(error = "Süre en az 1 dakika olmalı")
            return
        }

        _form.value = form.copy(isSaving = true, error = null)

        viewModelScope.launch {
            // Başlangıç zamanı: seçilen tarih + saat ayarı
            val startCal = Calendar.getInstance().apply {
                timeInMillis = form.selectedDateMs
                set(Calendar.HOUR_OF_DAY, form.startHour)
                set(Calendar.MINUTE, form.startMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startMs = startCal.timeInMillis
            val endMs = startMs + form.durationMinutes * 60_000L

            val session = SessionEntity(
                categoryId = form.selectedCategory.id,
                startTime = startMs,
                endTime = endMs,
                durationMinutes = form.durationMinutes,
                note = form.note.takeIf { it.isNotBlank() },
                isManualEntry = true
            )
            repository.addManualSession(session)
            _form.value = _form.value.copy(isSaving = false, isSaved = true)
            onSuccess()
        }
    }
}
