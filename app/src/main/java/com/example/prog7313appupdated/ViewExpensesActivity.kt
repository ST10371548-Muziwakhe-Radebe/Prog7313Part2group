package com.example.prog7313appupdated

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313appupdated.Adapter.ExpenseAdapter
import com.example.prog7313appupdated.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ViewExpensesActivity : AppCompatActivity() {

    private val TAG = "ViewExpensesActivity"
    private var userId: Int = -1
    
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var rvExpenses: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var btnBack: View

    // ISO Dates for Database querying (yyyy-MM-dd)
    private var startDate: String = "1900-01-01"
    private var endDate: String = "2100-01-01"

    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_expenses)

        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error returning data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnStartDate = findViewById(R.id.btnStartDate)
        btnEndDate = findViewById(R.id.btnEndDate)
        rvExpenses = findViewById(R.id.rvExpenses)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBack = findViewById(R.id.btnBack)

        rvExpenses.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener { finish() }

        // Date selection interaction
        setupDatePickers()

        // Load initially with all time
        loadExpenses()
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        btnStartDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                startDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                btnStartDate.text = startDate
                loadExpenses()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnEndDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                endDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                btnEndDate.text = endDate
                loadExpenses()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)

                // Fetch categories to safely map their Names
                val categories = db.categoryDao().getCategoriesByUser(userId)
                val categoryMap = categories.associate { it.categoryId to it.name }

                // Fetch expenses within specific dates
                val expenses = db.expenseEntryDao().getEntriesByPeriod(userId, startDate, endDate)

                withContext(Dispatchers.Main) {
                    if (expenses.isEmpty()) {
                        rvExpenses.visibility = View.GONE
                        tvEmptyState.visibility = View.VISIBLE
                    } else {
                        rvExpenses.visibility = View.VISIBLE
                        tvEmptyState.visibility = View.GONE
                        
                        if (!::adapter.isInitialized) {
                            adapter = ExpenseAdapter(expenses, categoryMap)
                            adapter.onReceiptClick = { path ->
                                showPhotoDialog(path)
                            }
                            rvExpenses.adapter = adapter
                        } else {
                            adapter.updateData(expenses)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
            }
        }
    }

    private fun showPhotoDialog(photoPath: String) {
        val imageView = ImageView(this).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adjustViewBounds = true
            setPadding(16, 16, 16, 16)
        }

        try {
            imageView.setImageURI(Uri.parse(photoPath))
            
            AlertDialog.Builder(this)
                .setTitle("Receipt Photograph")
                .setView(imageView)
                .setPositiveButton("Close", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
        }
    }
}