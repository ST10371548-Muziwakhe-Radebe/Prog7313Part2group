package com.example.prog7313appupdated

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Tag for logging
    private val TAG = "MainActivity"

    // Store the logged in user's ID
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        Log.d(TAG, "MainActivity created")

        // Get the userId passed from LoginActivity
        userId = intent.getIntExtra("userId", -1)
        Log.d(TAG, "Logged in userId: $userId")

        // Set welcome message
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Welcome to Budget Tracker!"

        // Navigate to Categories screen
        findViewById<Button>(R.id.btnCategories).setOnClickListener {
            Log.d(TAG, "Navigating to CategoryActivity")
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        // Navigate to Add Expense screen
        findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            Log.d(TAG, "Navigating to AddExpenseActivity")
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        // Navigate to View Expenses screen
        findViewById<Button>(R.id.btnViewExpenses).setOnClickListener {
            Log.d(TAG, "Navigating to ViewExpensesActivity")
            val intent = Intent(this, ViewExpensesActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        // Navigate to Set Goals screen
        findViewById<Button>(R.id.btnSetGoals).setOnClickListener {
            Log.d(TAG, "Navigating to SetGoalsActivity")
            val intent = Intent(this, SetGoalsActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        // Navigate to Category Totals screen
        findViewById<Button>(R.id.btnCategoryTotals).setOnClickListener {
            Log.d(TAG, "Navigating to CategoryTotalsActivity")
            val intent = Intent(this, CategoryTotalsActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }
}