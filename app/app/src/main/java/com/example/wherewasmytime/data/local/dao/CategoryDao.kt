package com.example.wherewasmytime.data.local.dao

import androidx.room.*
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    // --- Okuma İşlemleri ---

    /** Silinmemiş (aktif) tüm kategorileri döndürür. Ana ekran listesi için. */
    @Query("SELECT * FROM categories WHERE isArchived = 0 ORDER BY name ASC")
    fun getActiveCategories(): Flow<List<CategoryEntity>>

    /** İstatistikler için arşivlenenler dahil tüm kategorileri döndürür. */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /** ID ile tek kategori getirir. */
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    // --- Yazma İşlemleri ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    /**
     * Soft-Delete: Kategoriyi silmez, sadece isArchived = 1 yapar.
     * Bu sayede geçmiş oturumlar istatistiklerde kalmaya devam eder.
     */
    @Query("UPDATE categories SET isArchived = 1 WHERE id = :id")
    suspend fun archiveCategory(id: Long)

    /** Gerçekten silmek istenirse (normalde kullanılmaz). */
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun hardDeleteCategory(id: Long)
}
