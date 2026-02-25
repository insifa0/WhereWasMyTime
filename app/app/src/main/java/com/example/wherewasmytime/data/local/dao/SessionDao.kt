package com.example.wherewasmytime.data.local.dao

import androidx.room.*
import com.example.wherewasmytime.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    // --- Okuma İşlemleri ---

    /** Bugüne ait tüm oturumları getirir. */
    @Query("""
        SELECT * FROM sessions 
        WHERE startTime >= :dayStartMs 
        ORDER BY startTime DESC
    """)
    fun getSessionsForDay(dayStartMs: Long): Flow<List<SessionEntity>>

    /** Ana ekrandaki "Son Aktiviteler" için tarihten bağımsız son N kayıt. */
    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<SessionEntity>>

    /** Belirli tarih aralığındaki oturumları getirir (haftalık/aylık raporlar için). */
    @Query("""
        SELECT * FROM sessions 
        WHERE startTime >= :startMs AND startTime <= :endMs 
        ORDER BY startTime DESC
    """)
    fun getSessionsInRange(startMs: Long, endMs: Long): Flow<List<SessionEntity>>

    /** Belirli bir kategorinin tüm oturumlarını getirir (kategori detay ekranı için). */
    @Query("SELECT * FROM sessions WHERE categoryId = :categoryId ORDER BY startTime DESC")
    fun getSessionsByCategory(categoryId: Long): Flow<List<SessionEntity>>

    /** Aktif (devam eden) oturumu getirir. endTime null ise devam ediyor demektir. */
    @Query("SELECT * FROM sessions WHERE endTime IS NULL LIMIT 1")
    fun getActiveSession(): Flow<SessionEntity?>

    /** Tüm zamanların toplam dakikasını hesaplar. */
    @Query("SELECT SUM(durationMinutes) FROM sessions WHERE categoryId = :categoryId")
    fun getTotalMinutesForCategory(categoryId: Long): Flow<Int?>

    // --- Yazma İşlemleri ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()


    /** Kronometreyi durdurmak: endTime ve durationMinutes'i günceller. */
    @Query("""
        UPDATE sessions 
        SET endTime = :endTime, durationMinutes = :durationMinutes 
        WHERE id = :sessionId
    """)
    suspend fun finishSession(sessionId: Long, endTime: Long, durationMinutes: Int)
}
