package com.example.prog7313appupdated.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.prog7313appupdated.database.entities.Category

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesByUser(userId: Int): List<Category>

    @Query("DELETE FROM categories WHERE categoryId = :categoryId")
    suspend fun deleteCategory(categoryId: Int)

    @Query("UPDATE categories SET isLimitActive = :isActive, limitAmount = :amount WHERE categoryId = :categoryId")
    suspend fun updateCategoryLimit(categoryId: Int, isActive: Boolean, amount: Double)
}