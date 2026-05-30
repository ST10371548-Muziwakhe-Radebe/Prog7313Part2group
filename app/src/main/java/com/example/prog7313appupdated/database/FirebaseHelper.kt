package com.example.prog7313appupdated.database

import android.content.Context
import android.util.Log
import com.example.prog7313appupdated.database.entities.Category
import com.example.prog7313appupdated.database.entities.CategoryTotal
import com.example.prog7313appupdated.database.entities.ExpenseEntry
import com.example.prog7313appupdated.database.entities.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

/**
 * Helper class to handle database operations for Firebase and coordinate with local Room database.
 * This class allows the user to switch seamlessly between a Local database and Online Firebase Database.
 */
object FirebaseHelper {

    private const val TAG = "FirebaseHelper"
    private const val PREFS_NAME = "MoneyGoalsPrefs"
    private const val KEY_DATABASE_MODE = "useFirebaseDatabase"

    // Reference to Firebase Realtime Database
    private val databaseReference by lazy {
        try {
            FirebaseDatabase.getInstance().reference
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase Database reference: ${e.message}")
            null
        }
    }

    /**
     * Set the database mode (True = Online Firebase, False = Local RoomDB)
     */
    fun setDatabaseMode(context: Context, useFirebase: Boolean) {
        Log.d(TAG, "Setting database mode: Firebase=$useFirebase")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DATABASE_MODE, useFirebase).apply()
    }

    /**
     * Get the active database mode
     */
    fun isFirebaseEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val mode = prefs.getBoolean(KEY_DATABASE_MODE, false)
        Log.d(TAG, "Checking database mode: FirebaseEnabled=$mode")
        return mode
    }

    /**
     * Performs a complete data migration from local RoomDB to Online Firebase Database
     * for a given user. This fulfills the task: "Migrate all existing RoomDB data to Firebase."
     */
    suspend fun migrateRoomToFirebase(context: Context, userId: Int): Boolean {
        Log.d(TAG, "Migrating Room DB data to Firebase for userId: $userId")
        val db = AppDatabase.getDatabase(context)
        val ref = databaseReference ?: return false

        try {
            // 1. Migrate user profile
            val user = db.userDao().getUserById(userId)
            if (user != null) {
                Log.d(TAG, "Migrating user profile: ${user.username}")
                ref.child("users").child(userId.toString()).setValue(user).await()
            }

            // 2. Migrate categories
            val categories = db.categoryDao().getCategoriesByUser(userId)
            Log.d(TAG, "Migrating ${categories.size} categories to Firebase")
            for (category in categories) {
                ref.child("categories")
                    .child(userId.toString())
                    .child(category.categoryId.toString())
                    .setValue(category)
                    .await()
            }

            // 3. Migrate expense entries
            val entries = db.expenseEntryDao().getAllEntriesByUser(userId)
            Log.d(TAG, "Migrating ${entries.size} expense entries to Firebase")
            for (entry in entries) {
                ref.child("entries")
                    .child(userId.toString())
                    .child(entry.entryId.toString())
                    .setValue(entry)
                    .await()
            }

            Log.d(TAG, "Migration to Firebase completed successfully!")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating data to Firebase: ${e.message}", e)
            return false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // USER OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Insert or register a new user. Works for both Local and Online database modes.
     */
    suspend fun insertUser(context: Context, user: User): Boolean {
        val db = AppDatabase.getDatabase(context)
        
        // 1. Always insert into local database as a reliable offline cache/fallback
        db.userDao().insertUser(user)
        Log.d(TAG, "User inserted locally: ${user.username}")

        // 2. If online mode is enabled, write user to Firebase
        if (isFirebaseEnabled(context)) {
            val ref = databaseReference ?: return false
            try {
                // Find next user ID in Firebase if the local database is fresh
                val snapshot = ref.child("users").get().await()
                var nextId = user.userId
                if (nextId == 0) {
                    var maxId = 0
                    for (child in snapshot.children) {
                        val existing = child.getValue(User::class.java)
                        if (existing != null && existing.userId > maxId) {
                            maxId = existing.userId
                        }
                    }
                    nextId = maxId + 1
                }
                
                val finalUser = user.copy(userId = nextId)
                ref.child("users").child(nextId.toString()).setValue(finalUser).await()
                Log.d(TAG, "User registered in Firebase: ${finalUser.username} with ID $nextId")
            } catch (e: Exception) {
                Log.e(TAG, "Firebase User registration failed: ${e.message}", e)
                return false
            }
        }
        return true
    }

    /**
     * Update user credentials or details. Syncs to Firebase if enabled.
     */
    suspend fun updateUser(context: Context, user: User): Boolean {
        val db = AppDatabase.getDatabase(context)
        db.userDao().updateUser(user)
        Log.d(TAG, "User updated locally: ${user.username}")

        if (isFirebaseEnabled(context)) {
            val ref = databaseReference ?: return false
            try {
                ref.child("users").child(user.userId.toString()).setValue(user).await()
                Log.d(TAG, "User updated in Firebase: ${user.username}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update user in Firebase: ${e.message}", e)
                return false
            }
        }
        return true
    }

    /**
     * Perform login verification. If Firebase is active, we check Firebase first.
     */
    suspend fun login(context: Context, username: String, password: String): User? {
        if (isFirebaseEnabled(context)) {
            val ref = databaseReference ?: return null
            try {
                Log.d(TAG, "Attempting online login for username: $username")
                val snapshot = ref.child("users").get().await()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    if (user != null && user.username == username && user.password == password) {
                        Log.d(TAG, "Online login successful for user: ${user.userId}")
                        
                        // Sync to local database so local queries remain valid
                        val localDb = AppDatabase.getDatabase(context)
                        val existingLocal = localDb.userDao().getUserById(user.userId)
                        if (existingLocal == null) {
                            localDb.userDao().insertUser(user)
                        } else if (existingLocal != user) {
                            localDb.userDao().updateUser(user)
                        }
                        
                        return user
                    }
                }
                Log.w(TAG, "Online login failed for username: $username")
                return null
            } catch (e: Exception) {
                Log.e(TAG, "Firebase login failed, falling back to local verification: ${e.message}", e)
            }
        }

        // Local fallback / local mode
        Log.d(TAG, "Performing local login for username: $username")
        val db = AppDatabase.getDatabase(context)
        return db.userDao().login(username, password)
    }

    /**
     * Check if a username is already taken.
     */
    suspend fun getUserByUsername(context: Context, username: String): User? {
        if (isFirebaseEnabled(context)) {
            val ref = databaseReference
            if (ref != null) {
                try {
                    val snapshot = ref.child("users").get().await()
                    for (child in snapshot.children) {
                        val user = child.getValue(User::class.java)
                        if (user != null && user.username.equals(username, ignoreCase = true)) {
                            return user
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error querying username on Firebase: ${e.message}")
                }
            }
        }
        val db = AppDatabase.getDatabase(context)
        return db.userDao().getUserByUsername(username)
    }

    /**
     * Fetch user profile details by ID.
     */
    suspend fun getUserById(context: Context, userId: Int): User? {
        if (isFirebaseEnabled(context)) {
            val ref = databaseReference
            if (ref != null) {
                try {
                    val snapshot = ref.child("users").child(userId.toString()).get().await()
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        return user
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching user by ID on Firebase: ${e.message}")
                }
            }
        }
        val db = AppDatabase.getDatabase(context)
        return db.userDao().getUserById(userId)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CATEGORY OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Insert a new category. Syncs to Firebase if enabled.
     */
    suspend fun insertCategory(context: Context, category: Category): Boolean {
        val db = AppDatabase.getDatabase(context)
        db.categoryDao().insertCategory(category)
        Log.d(TAG, "Category inserted locally: ${category.name}")

        if (isFirebaseEnabled(context)) {
            val ref = databaseReference ?: return false
            try {
                // Fetch existing categories to determine next ID
                val snapshot = ref.child("categories").child(category.userId.toString()).get().await()
                var nextId = category.categoryId
                if (nextId == 0) {
                    var maxId = 0
                    for (child in snapshot.children) {
                        val existing = child.getValue(Category::class.java)
                        if (existing != null && existing.categoryId > maxId) {
                            maxId = existing.categoryId
                        }
                    }
                    nextId = maxId + 1
                }
                
                val finalCategory = category.copy(categoryId = nextId)
                ref.child("categories")
                    .child(category.userId.toString())
                    .child(nextId.toString())
                    .setValue(finalCategory)
                    .await()
                Log.d(TAG, "Category synced to Firebase: ${finalCategory.name} with ID $nextId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed syncing category to Firebase: ${e.message}", e)
                return false
            }
        }
        return true
    }

    /**
     * Load all categories for a specific user.
     */
    suspend fun getCategoriesByUser(context: Context, userId: Int): List<Category> {
        if (isFirebaseEnabled(context)) {
            val ref = databaseReference
            if (ref != null) {
                try {
                    Log.d(TAG, "Fetching categories from Firebase for userId: $userId")
                    val snapshot = ref.child("categories").child(userId.toString()).get().await()
                    val list = mutableListOf<Category>()
                    for (child in snapshot.children) {
                        val category = child.getValue(Category::class.java)
                        if (category != null) {
                            list.add(category)
                        }
                    }
                    Log.d(TAG, "Firebase found ${list.size} categories")
                    return list
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching categories from Firebase: ${e.message}")
                }
            }
        }
        Log.d(TAG, "Fetching categories from Room DB for userId: $userId")
        val db = AppDatabase.getDatabase(context)
        return db.categoryDao().getCategoriesByUser(userId)
    }

    /**
     * Delete a category. Syncs to Firebase if enabled.
     */
    suspend fun deleteCategory(context: Context, categoryId: Int, userId: Int): Boolean {
        val db = AppDatabase.getDatabase(context)
        db.categoryDao().deleteCategory(categoryId)
        Log.d(TAG, "Category deleted locally: $categoryId")

        if (isFirebaseEnabled(context)) {
            val ref = databaseReference ?: return false
            try {
                ref.child("categories")
                    .child(userId.toString())
                    .child(categoryId.toString())
                    .removeValue()
                    .await()
                Log.d(TAG, "Category deleted from Firebase: $categoryId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete category from Firebase: ${e.message}", e)
                return false
            }
        }
        return true
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EXPENSE ENTRY OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Insert a new expense entry. Syncs to Firebase if enabled.
     */
    suspend fun insertEntry(context: Context, entry: ExpenseEntry): Boolean {
        val db = AppDatabase.getDatabase(context)
        db.expenseEntryDao().insertEntry(entry)
        Log.d(TAG, "Expense entry inserted locally: ${entry.description}")

        if (isFirebaseEnabled(context)) {
            val ref = databaseReference ?: return false
            try {
                // Fetch existing entries to find the next ID
                val snapshot = ref.child("entries").child(entry.userId.toString()).get().await()
                var nextId = entry.entryId
                if (nextId == 0) {
                    var maxId = 0
                    for (child in snapshot.children) {
                        val existing = child.getValue(ExpenseEntry::class.java)
                        if (existing != null && existing.entryId > maxId) {
                            maxId = existing.entryId
                        }
                    }
                    nextId = maxId + 1
                }
                
                val finalEntry = entry.copy(entryId = nextId)
                ref.child("entries")
                    .child(entry.userId.toString())
                    .child(nextId.toString())
                    .setValue(finalEntry)
                    .await()
                Log.d(TAG, "Expense entry synced to Firebase: ${finalEntry.description} with ID $nextId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed syncing expense entry to Firebase: ${e.message}", e)
                return false
            }
        }
        return true
    }

    /**
     * Load expenses within a selectable period.
     */
    suspend fun getEntriesByPeriod(
        context: Context,
        userId: Int,
        startDate: String,
        endDate: String
    ): List<ExpenseEntry> {
        if (isFirebaseEnabled(context)) {
            val ref = databaseReference
            if (ref != null) {
                try {
                    Log.d(TAG, "Fetching expenses from Firebase in range [$startDate, $endDate] for userId: $userId")
                    val snapshot = ref.child("entries").child(userId.toString()).get().await()
                    val list = mutableListOf<ExpenseEntry>()
                    for (child in snapshot.children) {
                        val entry = child.getValue(ExpenseEntry::class.java)
                        if (entry != null && entry.date >= startDate && entry.date <= endDate) {
                            list.add(entry)
                        }
                    }
                    Log.d(TAG, "Firebase found ${list.size} matching expenses")
                    return list
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching expenses from Firebase: ${e.message}")
                }
            }
        }
        Log.d(TAG, "Fetching expenses from Room DB in range [$startDate, $endDate] for userId: $userId")
        val db = AppDatabase.getDatabase(context)
        return db.expenseEntryDao().getEntriesByPeriod(userId, startDate, endDate)
    }

    /**
     * View the total amount spent on each category during a selectable period.
     */
    suspend fun getCategoryTotals(
        context: Context,
        userId: Int,
        startDate: String,
        endDate: String
    ): List<CategoryTotal> {
        if (isFirebaseEnabled(context)) {
            try {
                Log.d(TAG, "Calculating online category totals for userId: $userId")
                val entries = getEntriesByPeriod(context, userId, startDate, endDate)
                
                // Group entries by category ID and calculate sum of amounts
                val totalsMap = entries.groupBy { it.categoryId }
                    .mapValues { entryGroup -> entryGroup.value.sumOf { it.amount } }

                val totalsList = totalsMap.map { (catId, sum) ->
                    CategoryTotal(categoryId = catId, total = sum)
                }
                Log.d(TAG, "Online calculation found ${totalsList.size} category totals")
                return totalsList
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating category totals online: ${e.message}")
            }
        }
        Log.d(TAG, "Calculating local category totals from Room DB for userId: $userId")
        val db = AppDatabase.getDatabase(context)
        return db.expenseEntryDao().getCategoryTotals(userId, startDate, endDate)
    }
}
