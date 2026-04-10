package com.example.prog7313appupdated.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prog7313appupdated.database.dao.CategoryDao
import com.example.prog7313appupdated.database.dao.ExpenseEntryDao
import com.example.prog7313appupdated.database.dao.UserDao
import com.example.prog7313appupdated.database.entities.Category
import com.example.prog7313appupdated.database.entities.ExpenseEntry
import com.example.prog7313appupdated.database.entities.User

// Tag for logging
private const val TAG = "AppDatabase"

// Define the database with all entities and version number
@Database(
    entities = [User::class, Category::class, ExpenseEntry::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs for accessing each table
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseEntryDao(): ExpenseEntryDao

    companion object {
        // Volatile ensures INSTANCE is always up to date across all threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Get or create the database instance
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Creating new database instance")

                // Build the RoomDB database
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_tracker_db" // Database file name
                )
                    .fallbackToDestructiveMigration() // Allows Room to destructive recreate database tables if migrations are missing
                    .build()

                INSTANCE = instance
                Log.d(TAG, "Database instance created successfully")
                instance
            }
        }
    }
}