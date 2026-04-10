package com.example.prog7313appupdated

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313appupdated.database.AppDatabase
import com.example.prog7313appupdated.database.entities.Category
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        userId = intent.getIntExtra("userId", -1)

        val db = AppDatabase.getDatabase(this)
        val etCategoryName = findViewById<EditText>(R.id.etCategoryName)
        val btnSaveCategory = findViewById<Button>(R.id.btnSaveCategory)
        val lvCategories = findViewById<ListView>(R.id.lvCategories)
        val spinnerCategoryColor = findViewById<Spinner>(R.id.spinnerCategoryColor)
// Colour options
        val colorOptions = listOf("Green", "Red", "Blue", "Orange", "Purple")
        val colorValues = listOf("#43A047", "#E53935", "#1E88E5", "#FB8C00", "#8E24AA")

        spinnerCategoryColor.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            colorOptions
        )
        // Load and display existing categories
        fun loadCategories() {
            lifecycleScope.launch {
                val categories = db.categoryDao().getCategoriesByUser(userId)
                val displayNames = categories.map { "${it.name} (${it.color})" }
                runOnUiThread {
                    lvCategories.adapter = ArrayAdapter(
                        this@CategoryActivity,
                        android.R.layout.simple_list_item_1,
                        displayNames
                    )
                }
            }
        }

        loadCategories()

        // Save new category
        btnSaveCategory.setOnClickListener {
            val name = etCategoryName.text.toString().trim()

            if (name.isEmpty()) {
                etCategoryName.error = "Please enter a category name"
                return@setOnClickListener
            }

            val selectedColor = colorValues[spinnerCategoryColor.selectedItemPosition]

            lifecycleScope.launch {
                val category = Category(
                    name = name,
                    userId = userId,
                    color = selectedColor
                )
                db.categoryDao().insertCategory(category)
                android.util.Log.d(
                    "CategoryActivity",
                    "Category saved: $name, color: $selectedColor, userId: $userId"
                )
                runOnUiThread {
                    etCategoryName.text.clear()
                    Toast.makeText(
                        this@CategoryActivity,
                        "Category '$name' saved!",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadCategories()
                }
            }
        }
    }
}