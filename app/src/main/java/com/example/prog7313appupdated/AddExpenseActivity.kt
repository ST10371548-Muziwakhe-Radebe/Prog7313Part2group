package com.example.prog7313appupdated

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.prog7313appupdated.database.AppDatabase
import com.example.prog7313appupdated.database.entities.Category
import com.example.prog7313appupdated.database.entities.ExpenseEntry
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class AddExpenseActivity : AppCompatActivity() {

    private var userId: Int = -1
    private var selectedDate = ""
    private var startTime = ""
    private var endTime = ""
    private var photoUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var categories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        userId = intent.getIntExtra("userId", -1)
        android.util.Log.d("AddExpenseActivity", "Loaded with userId: $userId")

        val db = AppDatabase.getDatabase(this)

        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etAmount = findViewById<EditText>(R.id.etExpenseAmount)
        val btnPickDate = findViewById<View>(R.id.btnPickDate)
        val tvSelectedDate = findViewById<TextView>(R.id.tvSelectedDate)
        val btnStartTime = findViewById<View>(R.id.btnStartTime)
        val tvStartTime = findViewById<TextView>(R.id.tvStartTime)
        val btnEndTime = findViewById<View>(R.id.btnEndTime)
        val tvEndTime = findViewById<TextView>(R.id.tvEndTime)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val btnAddPhoto = findViewById<View>(R.id.btnAddPhoto)
        val ivPhotoPreview = findViewById<ImageView>(R.id.ivPhotoPreview)
        val btnSaveEntry = findViewById<Button>(R.id.btnSaveExpense)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // ── CAMERA LAUNCHER ──────────────────────────────────────────
        val takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && cameraImageUri != null) {
                photoUri = cameraImageUri
                ivPhotoPreview.setImageURI(cameraImageUri)
                ivPhotoPreview.visibility = View.VISIBLE
                android.util.Log.d("AddExpenseActivity", "Photo taken: $cameraImageUri")
                Toast.makeText(this, "Photo captured!", Toast.LENGTH_SHORT).show()
            }
        }

        // ── GALLERY LAUNCHER ─────────────────────────────────────────
        val pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                photoUri = uri
                ivPhotoPreview.setImageURI(uri)
                ivPhotoPreview.visibility = View.VISIBLE
                android.util.Log.d("AddExpenseActivity", "Photo from gallery: $uri")
                Toast.makeText(this, "Photo selected!", Toast.LENGTH_SHORT).show()
            }
        }

        // ── LOAD CATEGORIES ──────────────────────────────────────────
        lifecycleScope.launch {
            categories = db.categoryDao().getCategoriesByUser(userId)
            val names = categories.map { it.name }
            runOnUiThread {
                if (names.isEmpty()) {
                    Toast.makeText(
                        this@AddExpenseActivity,
                        "Please create a category first!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                spinnerCategory.adapter = ArrayAdapter(
                    this@AddExpenseActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    names
                )
            }
        }

        // ── DATE PICKER ──────────────────────────────────────────────
        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                selectedDate = "$y.${String.format("%02d", m + 1)}.${
                    String.format("%02d", d)}"
                tvSelectedDate.text = selectedDate
                android.util.Log.d("AddExpenseActivity", "Date: $selectedDate")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // ── START TIME ───────────────────────────────────────────────
        btnStartTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                startTime = "${String.format("%02d", h)}:${String.format("%02d", m)}"
                tvStartTime.text = startTime
                android.util.Log.d("AddExpenseActivity", "Start time: $startTime")
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        // ── END TIME ─────────────────────────────────────────────────
        btnEndTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                endTime = "${String.format("%02d", h)}:${String.format("%02d", m)}"
                tvEndTime.text = endTime
                android.util.Log.d("AddExpenseActivity", "End time: $endTime")
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        // ── PHOTO — Camera or Gallery ────────────────────────────────
        btnAddPhoto.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Add Receipt Photo")
                .setItems(arrayOf("📷  Take Photo", "🖼️  Choose from Gallery")) { _, which ->
                    when (which) {
                        0 -> {
                            try {
                                val photoFile = File(
                                    getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                    "receipt_${System.currentTimeMillis()}.jpg"
                                )
                                cameraImageUri = FileProvider.getUriForFile(
                                    this,
                                    "${packageName}.fileprovider",
                                    photoFile
                                )
                                takePictureLauncher.launch(cameraImageUri!!)
                            } catch (e: Exception) {
                                android.util.Log.e("AddExpenseActivity", "Camera error: ${e.message}")
                                Toast.makeText(this, "Camera not available",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                        1 -> pickImageLauncher.launch("image/*")
                    }
                }.show()
        }

        // ── CANCEL ───────────────────────────────────────────────────
        btnCancel.setOnClickListener { finish() }

        // ── SAVE ─────────────────────────────────────────────────────
        btnSaveEntry.setOnClickListener {
            val description = etDescription.text.toString().trim()
            val amountText = etAmount.text.toString().trim()

            if (amountText.isEmpty() || amountText.toDoubleOrNull() == null
                || amountText.toDouble() <= 0) {
                etAmount.error = "Please enter a valid amount"
                return@setOnClickListener
            }
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startTime.isEmpty()) {
                Toast.makeText(this, "Please select a start time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (endTime.isEmpty()) {
                Toast.makeText(this, "Please select an end time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (categories.isEmpty()) {
                Toast.makeText(this, "Please create a category first",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCategory = categories[spinnerCategory.selectedItemPosition]

            lifecycleScope.launch {
                val entry = ExpenseEntry(
                    amount = amountText.toDouble(),
                    date = selectedDate,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    categoryId = selectedCategory.categoryId,
                    userId = userId,
                    photoPath = photoUri?.toString()
                )
                db.expenseEntryDao().insertEntry(entry)
                android.util.Log.d(
                    "AddExpenseActivity",
                    "Entry saved — desc: $description, amount: $amountText, " +
                            "category: ${selectedCategory.name}, photo: ${photoUri?.toString()}"
                )
                runOnUiThread {
                    Toast.makeText(this@AddExpenseActivity,
                        "Expense saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
