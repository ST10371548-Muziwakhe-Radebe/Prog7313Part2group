package com.example.prog7313appupdated.database.dao

// Kotlin
data class Achievement(val title: String, val iconRes: Int)

// Java
public class Achievement {
    private String title;
    private int iconRes;
    public Achievement(String title, int iconRes) {
        this.title = title;
        this.iconRes = iconRes;
    }
    // Getters...
}
