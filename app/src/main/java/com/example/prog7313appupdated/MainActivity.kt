package com.example.prog7313appupdated

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        userId = intent.getIntExtra("userId", -1)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Welcome to Budget Tracker!"

        findViewById<Button>(R.id.btnCategories).setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnViewExpenses).setOnClickListener {
            val intent = Intent(this, ViewExpensesActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSetGoals).setOnClickListener {
            val intent = Intent(this, SetGoalsActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnCategoryTotals).setOnClickListener {
            val intent = Intent(this, CategoryTotalsActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }
}