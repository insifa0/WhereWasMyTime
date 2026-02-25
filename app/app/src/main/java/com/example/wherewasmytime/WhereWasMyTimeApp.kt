package com.example.wherewasmytime

import android.app.Application
import com.example.wherewasmytime.data.local.AppDatabase
import com.example.wherewasmytime.data.preferences.UserPreferences
import com.example.wherewasmytime.data.repository.TimeRepository

/**
 * Uygulama genelinde tek bir veritabanı instance'ı, repository ve kullanıcı tercihleri tutar.
 * ViewModel'lar buradan erişebilir.
 */
class WhereWasMyTimeApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy {
        TimeRepository(
            categoryDao = database.categoryDao(),
            sessionDao = database.sessionDao()
        )
    }

    val userPreferences by lazy { UserPreferences(this) }
}

