package com.example.prog7313appupdated.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Int,
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    @androidx.room.ColumnInfo(defaultValue = "2000.0")
    val minBudgetGoal: Double = 2000.0,
    @androidx.room.ColumnInfo(defaultValue = "10000.0")
    val maxBudgetGoal: Double = 10000.0
)