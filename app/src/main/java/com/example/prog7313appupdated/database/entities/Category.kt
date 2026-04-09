package com.example.prog7313appupdated.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Int = 0,
    val name: String,
    val userId: Int,
    @androidx.room.ColumnInfo(defaultValue = "0")
    var isLimitActive: Boolean = false,
    @androidx.room.ColumnInfo(defaultValue = "0.0")
    var limitAmount: Double = 0.0
)