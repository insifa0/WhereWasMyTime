package com.example.wherewasmytime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Kategori tablosu.
 * isArchived = true ise kullanıcı bu kategoriyi "silmiş" demektir,
 * ama eski oturumlar istatistiklerde görünmeye devam eder (Soft-Delete).
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    /** Hex renk kodu, örn: "#13EC5B" */
    val color: String,

    /** Material symbol adı, örn: "code", "fitness_center". Null ise fotoğraf kullanılır. */
    val iconName: String? = null,

    /** Kullanıcının seçtiği özel fotoğrafın yerel dosya yolu. Null ise ikon kullanılır. */
    val photoPath: String? = null,

    /** Günlük hedef (dakika cinsinden). Null ise hedef yok. */
    val dailyGoalMinutes: Int? = null,

    /** true ise kullanıcı bu kategoriyi silmiş sayılır, listede gösterilmez. */
    val isArchived: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)
