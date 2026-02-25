package com.example.wherewasmytime.ui.screens.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wherewasmytime.WhereWasMyTimeApp
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Renkler: kullanıcı seçebileceği 12 preset renk
val PRESET_COLORS = listOf(
    "#13ec5b", // yeşil (primary)
    "#3B82F6", // mavi
    "#F59E0B", // sarı
    "#EF4444", // kırmızı
    "#8B5CF6", // mor
    "#EC4899", // pembe
    "#06B6D4", // cyan
    "#F97316", // turuncu
    "#10B981", // teal
    "#6366F1", // indigo
    "#84CC16", // lime
    "#E11D48"  // rose
)

data class ManageCategoriesUiState(
    val activeCategories: List<CategoryEntity> = emptyList(),
    val archivedCategories: List<CategoryEntity> = emptyList(),
    val editingCategory: CategoryEntity? = null,     // null = yeni ekleme, dolu = düzenleme
    val showSheet: Boolean = false
)

class ManageCategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as WhereWasMyTimeApp).repository

    private val _showSheet = MutableStateFlow(false)
    private val _editing = MutableStateFlow<CategoryEntity?>(null)

    val uiState: StateFlow<ManageCategoriesUiState> = combine(
        repository.getActiveCategories(),
        repository.getAllCategories(),
        _showSheet,
        _editing
    ) { active, all, show, editing ->
        val archived = all.filter { it.isArchived }
        ManageCategoriesUiState(
            activeCategories = active,
            archivedCategories = archived,
            editingCategory = editing,
            showSheet = show
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ManageCategoriesUiState())

    fun startAdding() {
        _editing.value = null
        _showSheet.value = true
    }

    fun startEditing(category: CategoryEntity) {
        _editing.value = category
        _showSheet.value = true
    }

    fun closeSheet() {
        _showSheet.value = false
        _editing.value = null
    }

    fun saveCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            val existing = _editing.value
            if (existing == null) {
                // Yeni kategori ekle
                repository.addCategory(
                    CategoryEntity(name = name.trim(), color = colorHex)
                )
            } else {
                // Var olanı güncelle
                repository.updateCategory(existing.copy(name = name.trim(), color = colorHex))
            }
            closeSheet()
        }
    }

    fun archiveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category.id)
        }
    }

    fun restoreCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category.copy(isArchived = false))
        }
    }
}
