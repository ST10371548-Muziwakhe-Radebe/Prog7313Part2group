package com.example.prog7313appupdated.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true)
    val entryId: Int = 0,
    val amount: Double,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val categoryId: Int,
    val userId: Int,
    val photoPath: String? = null
)