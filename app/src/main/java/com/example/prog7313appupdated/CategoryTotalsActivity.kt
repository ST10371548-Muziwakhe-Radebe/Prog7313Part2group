package com.example.prog7313appupdated

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313appupdated.Adapter.CategoryTotalAdapter
import com.example.prog7313appupdated.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class CategoryTotalsActivity : AppCompatActivity() {

    private val TAG = "CategoryTotalsActivity"
    private var userId: Int = -1

    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var rvCategoryTotals: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var btnBack: View

    private var startDate: String = "1900-01-01"
    private var endDate: String = "2100-01-01"

    private lateinit var adapter: CategoryTotalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_totals)

        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error returning data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnStartDate = findViewById(R.id.btnStartDate)
        btnEndDate = findViewById(R.id.btnEndDate)
        rvCategoryTotals = findViewById(R.id.rvCategoryTotals)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBack = findViewById(R.id.btnBack)

        rvCategoryTotals.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener { finish() }

        setupDatePickers()
        loadCategoryTotals()
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        btnStartDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                startDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                btnStartDate.text = startDate
                loadCategoryTotals()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnEndDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                endDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                btnEndDate.text = endDate
                loadCategoryTotals()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun loadCategoryTotals() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)

                val categories = db.categoryDao().getCategoriesByUser(userId)
                val categoryMap = categories.associate { it.categoryId to it.name }

                val totals = db.expenseEntryDao().getCategoryTotals(userId, startDate, endDate)

                withContext(Dispatchers.Main) {
                    if (totals.isEmpty()) {
                        rvCategoryTotals.visibility = View.GONE
                        tvEmptyState.visibility = View.VISIBLE
                    } else {
                        rvCategoryTotals.visibility = View.VISIBLE
                        tvEmptyState.visibility = View.GONE
                        
                        if (!::adapter.isInitialized) {
                            adapter = CategoryTotalAdapter(totals, categoryMap)
                            rvCategoryTotals.adapter = adapter
                        } else {
                            adapter.updateData(totals, categoryMap)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading category totals: ${e.message}")
            }
        }
    }
}
