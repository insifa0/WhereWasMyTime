package com.example.wherewasmytime.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Oturum (zaman kaydı) tablosu.
 * Her kayıt bir kategoriye bağlıdır (ForeignKey).
 * Kategori silinirse (soft-delete) oturum yine de varlığını sürdürür.
 */
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            // Kategori hard-delete edilirse (normalde olmaz) oturumu koru, categoryId null yap
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Bağlı olduğu kategori ID'si. Kategori silinirse null olur. */
    val categoryId: Long?,

    /** Oturumun başlangıç zamanı (Unix timestamp, ms) */
    val startTime: Long,

    /** Oturumun bitiş zamanı (Unix timestamp, ms). Aktif oturumda null. */
    val endTime: Long? = null,

    /** Toplam süre (dakika cinsinden). Bitiş sonrası hesaplanıp yazılır. */
    val durationMinutes: Int = 0,

    /** Kullanıcının oturuma eklediği fotoğrafın yerel yolu. */
    val photoPath: String? = null,

    /** Kullanıcının oturuma eklediği kısa not. */
    val note: String? = null,

    /**
     * Oturumun kronometreyle mi yoksa manuel mi girildiği.
     * false = kronometre, true = manuel giriş
     */
    val isManualEntry: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)
