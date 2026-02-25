package com.example.wherewasmytime.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.wherewasmytime.data.local.dao.CategoryDao
import com.example.wherewasmytime.data.local.dao.SessionDao
import com.example.wherewasmytime.data.local.entity.CategoryEntity
import com.example.wherewasmytime.data.local.entity.SessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CategoryEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wherewasmytime.db"
                )
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Uygulama ilk açıldığında varsayılan kategorileri ekler.
     */
    private class PrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    database.categoryDao().insertCategory(
                        CategoryEntity(name = "Ders", color = "#3B82F6", iconName = "menu_book")
                    )
                    database.categoryDao().insertCategory(
                        CategoryEntity(name = "Programlama", color = "#13EC5B", iconName = "code")
                    )
                    database.categoryDao().insertCategory(
                        CategoryEntity(name = "Spor", color = "#EF4444", iconName = "fitness_center")
                    )
                    database.categoryDao().insertCategory(
                        CategoryEntity(name = "Sosyal Medya", color = "#A855F7", iconName = "share")
                    )
                    database.categoryDao().insertCategory(
                        CategoryEntity(name = "Dinlenme", color = "#F59E0B", iconName = "weekend")
                    )
                }
            }
        }
    }
}
