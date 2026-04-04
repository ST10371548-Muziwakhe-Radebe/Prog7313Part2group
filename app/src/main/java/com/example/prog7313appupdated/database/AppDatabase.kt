package com.example.prog7313appupdated.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prog7313appupdated.database.dao.CategoryDao
import com.example.prog7313appupdated.database.dao.ExpenseEntryDao
import com.example.prog7313appupdated.database.dao.UserDao
import com.example.prog7313appupdated.database.entities.Category
import com.example.prog7313appupdated.database.entities.ExpenseEntry
import com.example.prog7313appupdated.database.entities.User

@Database(
    entities = [User::class, Category::class, ExpenseEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseEntryDao(): ExpenseEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}