package com.example.wherewasmytime.data.repository

import com.example.wherewasmytime.data.local.dao.CategoryDao
import com.example.wherewasmytime.data.local.dao.SessionDao
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import com.example.wherewasmytime.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Uygulama genelindeki tüm veri erişimi bu repository üzerinden yapılır.
 * ViewModel'lar doğrudan DAO'ya değil, buraya bağlanır.
 * İleride Firebase eklenirse buradaki implementasyon değişir, ViewModel değişmez.
 */
class TimeRepository(
    private val categoryDao: CategoryDao,
    private val sessionDao: SessionDao
) {

    // =====================
    // Kategori İşlemleri
    // =====================

    fun getActiveCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getActiveCategories()

    fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): CategoryEntity? =
        categoryDao.getCategoryById(id)

    suspend fun addCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    /** Soft-delete: Kategoriyi arşivler, geçmiş kayıtları korur. */
    suspend fun archiveCategory(id: Long) =
        categoryDao.archiveCategory(id)

    /** Hard-delete: Kategoriyi ve gerekirse altındaki verileri tamamen siler. */
    suspend fun hardDeleteCategory(id: Long) =
        categoryDao.hardDeleteCategory(id)

    // =====================
    // Oturum İşlemleri
    // =====================

    fun getActiveSession(): Flow<SessionEntity?> =
        sessionDao.getActiveSession()

    /** Ana ekran "Son Aktiviteler": tarihten bağımsız son 10 kayıt */
    fun getRecentSessions(limit: Int = 10): Flow<List<SessionEntity>> =
        sessionDao.getRecentSessions(limit)

    fun getTodaySessions(): Flow<List<SessionEntity>> {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return sessionDao.getSessionsForDay(startOfDay)
    }

    fun getSessionsInRange(startMs: Long, endMs: Long): Flow<List<SessionEntity>> =
        sessionDao.getSessionsInRange(startMs, endMs)

    fun getSessionsByCategory(categoryId: Long): Flow<List<SessionEntity>> =
        sessionDao.getSessionsByCategory(categoryId)

    /** Yeni bir oturum başlatır. Başlangıç zamanını şu anki zaman olarak kaydeder. */
    suspend fun startSession(categoryId: Long): Long {
        val session = SessionEntity(
            categoryId = categoryId,
            startTime = System.currentTimeMillis()
        )
        return sessionDao.insertSession(session)
    }

    /** Aktif oturumu bitirir: bitiş zamanını ve toplam dakikayı hesaplar. */
    suspend fun finishSession(sessionId: Long, startTime: Long) {
        val endTime = System.currentTimeMillis()
        val durationMinutes = ((endTime - startTime) / 1000 / 60).toInt()
        sessionDao.finishSession(sessionId, endTime, durationMinutes)
    }

    /** Manuel giriş: Geçmiş bir oturumu doğrudan kaydeder. */
    suspend fun addManualSession(session: SessionEntity): Long =
        sessionDao.insertSession(session.copy(isManualEntry = true))

    /** Undo veya tam oturum kaydetmek için kullanılır. */
    suspend fun insertSession(session: SessionEntity): Long =
        sessionDao.insertSession(session)

    suspend fun updateSession(session: SessionEntity) =
        sessionDao.updateSession(session)

    suspend fun deleteSession(id: Long) =
        sessionDao.deleteSession(id)

    suspend fun deleteAllSessions() =
        sessionDao.deleteAllSessions()
}

