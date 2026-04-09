package com.example.prog7313appupdated

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AddExpenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnCancel)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnSaveExpense)?.setOnClickListener {
            // TODO: Save logic, then finish
            finish()
        }
    }
}
