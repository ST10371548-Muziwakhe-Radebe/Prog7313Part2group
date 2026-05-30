package com.example.prog7313appupdated.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Int = 0,
    val name: String = "",
    val userId: Int = 0,
    val color: String = "#4CAF50"
)