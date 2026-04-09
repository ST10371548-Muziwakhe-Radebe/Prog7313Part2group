package com.example.prog7313appupdated

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313appupdated.database.AppDatabase
import com.example.prog7313appupdated.database.entities.Category
import com.example.prog7313appupdated.database.entities.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.RangeSlider
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetGoalsActivity : AppCompatActivity() {

    private val TAG = "SetGoalsActivity"
    private var userId: Int = -1
    private lateinit var db: AppDatabase
    private var currentUser: User? = null
    private var categoryList: List<Category> = emptyList()
    
    // UI References
    private lateinit var budgetRangeSlider: RangeSlider
    private lateinit var tvBudgetRangeLabel: TextView
    private lateinit var categoriesContainer: LinearLayout
    private lateinit var tvActiveLimits: TextView
    private lateinit var btnSaveGoals: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_goals)

        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = AppDatabase.getDatabase(this)

        budgetRangeSlider = findViewById(R.id.budgetRangeSlider)
        tvBudgetRangeLabel = findViewById(R.id.tvBudgetRangeLabel)
        categoriesContainer = findViewById(R.id.categoriesContainer)
        tvActiveLimits = findViewById(R.id.tvActiveLimits)
        btnSaveGoals = findViewById(R.id.btnSaveGoals)
        
        // Listen to Slider Value Changes
        budgetRangeSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            val min = values[0].toInt()
            val max = values[1].toInt()
            tvBudgetRangeLabel.text = "R$min — R$max"
        }

        // Save Button logic
        btnSaveGoals.setOnClickListener {
            saveGoalsToDatabase()
        }

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                // Fetch User
                currentUser = db.userDao().getUserById(userId)
                
                // Fetch Categories
                categoryList = db.categoryDao().getCategoriesByUser(userId)

                withContext(Dispatchers.Main) {
                    populateUI()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data: ${e.message}")
            }
        }
    }

    private fun populateUI() {
        // Set slider values
        val minGoal = currentUser?.minBudgetGoal?.toFloat() ?: 2000f
        val maxGoal = currentUser?.maxBudgetGoal?.toFloat() ?: 6000f
        
        // Ensure values fall within bounds
        val safeMin = maxOf(2000f, minOf(minGoal, 10000f))
        val safeMax = minOf(10000f, maxOf(maxGoal, safeMin))
        
        budgetRangeSlider.values = listOf(safeMin, safeMax)
        tvBudgetRangeLabel.text = "R${safeMin.toInt()} — R${safeMax.toInt()}"

        // Populate Categories
        categoriesContainer.removeAllViews()
        var activeLimitsCount = 0

        for (category in categoryList) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_category_limit, categoriesContainer, false)
            
            val tvCategoryName = view.findViewById<TextView>(R.id.tvCategoryName)
            val tvCategoryLimit = view.findViewById<TextView>(R.id.tvCategoryLimit)
            val switchCategoryLimit = view.findViewById<SwitchMaterial>(R.id.switchCategoryLimit)
            val iconContainer = view.findViewById<android.widget.FrameLayout>(R.id.iconContainer)

            tvCategoryName.text = category.name
            
            if (category.isLimitActive) {
                tvCategoryLimit.text = "R${category.limitAmount.toInt()} monthly"
                tvCategoryLimit.setTextColor(getColor(R.color.text_primary))
                switchCategoryLimit.isChecked = true
                activeLimitsCount++
            } else {
                tvCategoryLimit.text = "Not set"
                tvCategoryLimit.setTextColor(getColor(R.color.text_secondary))
                switchCategoryLimit.isChecked = false
            }

            // Optional Icon Tinting based on first letter or ID for variety
            val colors = listOf(R.color.icon_bg_blue, R.color.icon_bg_orange, R.color.icon_bg_purple, R.color.icon_bg_pink)
            val colorRes = colors[category.categoryId % colors.size]
            iconContainer.backgroundTintList = getColorStateList(colorRes)

            // Switch logic
            switchCategoryLimit.setOnCheckedChangeListener { _, isChecked ->
                category.isLimitActive = isChecked
                if (isChecked) {
                    // Pre-fill a limit amount if it was 0 for demonstration purposes
                    if (category.limitAmount == 0.0) category.limitAmount = 1000.0
                    tvCategoryLimit.text = "R${category.limitAmount.toInt()} monthly"
                    tvCategoryLimit.setTextColor(getColor(R.color.text_primary))
                } else {
                    tvCategoryLimit.text = "Not set"
                    tvCategoryLimit.setTextColor(getColor(R.color.text_secondary))
                }
                updateActiveLimitsCount()
            }

            // Let the user adjust limit amount directly? Currently mock as per instruction
            // For now, it stays static but active.

            categoriesContainer.addView(view)
        }
        
        tvActiveLimits.text = "${activeLimitsCount} ACTIVE"
    }

    private fun updateActiveLimitsCount() {
        val activeCount = categoryList.count { it.isLimitActive }
        tvActiveLimits.text = "${activeCount} ACTIVE"
    }

    private fun saveGoalsToDatabase() {
        val values = budgetRangeSlider.values
        val minGoal = values[0].toDouble()
        val maxGoal = values[1].toDouble()

        lifecycleScope.launch {
            try {
                // Update User
                db.userDao().updateUserBudgets(userId, minGoal, maxGoal)

                // Update Categories
                for (category in categoryList) {
                    db.categoryDao().updateCategoryLimit(category.categoryId, category.isLimitActive, category.limitAmount)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SetGoalsActivity, "Goals Saved Successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Return to previous screen
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving goals: ${e.message}")
            }
        }
    }
}
