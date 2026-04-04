package com.example.prog7313appupdated.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.prog7313appupdated.database.entities.ExpenseEntry

@Dao
interface ExpenseEntryDao {
    @Insert
    suspend fun insertEntry(entry: ExpenseEntry)

    @Query("SELECT * FROM entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getEntriesByPeriod(userId: Int, startDate: String, endDate: String): List<ExpenseEntry>

    @Query("SELECT categoryId, SUM(amount) as total FROM entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate GROUP BY categoryId")
    suspend fun getCategoryTotals(userId: Int, startDate: String, endDate: String): List<CategoryTotal>
}

data class CategoryTotal(
    val categoryId: Int,
    val total: Double
)