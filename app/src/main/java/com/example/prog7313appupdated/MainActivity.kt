package com.example.prog7313appupdated

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313appupdated.Adapter.AchievementAdapter
import com.example.prog7313appupdated.database.FirebaseHelper
import com.example.prog7313appupdated.database.entities.Achievement
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Safely handle UserId
        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            android.util.Log.e(TAG, "No userId provided! Returning to Login.")
            finish() // Close activity to prevent crash
            return
        }

        // 2. Setup RecyclerView safely
        val achievements = listOf(
            Achievement("Saver Starter", R.drawable.ic_goals),
            Achievement("Trick Streak", R.drawable.ic_fire),
            Achievement("Budget King", R.drawable.ic_wallet_logo),
            Achievement("Newbie", R.drawable.ic_fire)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.rvAchievements)
        recyclerView?.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                this@MainActivity,
                androidx.recyclerview.widget.RecyclerView.HORIZONTAL,
                false
            )
            adapter = AchievementAdapter(achievements)
        }

        // 3. Setup UI components
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        lifecycleScope.launch {
            try {
                val user = FirebaseHelper.getUserById(applicationContext, userId)

                user?.let {
                    val displayName = if (it.firstName.isNotEmpty()) it.firstName else it.username
                    tvWelcome?.text = "Hello, $displayName! 👋"
                } ?: run {
                    tvWelcome?.text = "Hello! 👋"
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Database error: ${e.message}")
            }
        }

        // 4. Floating Action Button
        findViewById<FloatingActionButton>(R.id.btnAddExpense)?.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        // 5. Drawer Navigation Setup
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        findViewById<ImageView>(R.id.btnMenu)?.setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)
        }

        navView?.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already at home
                }
                R.id.nav_add_expense -> {
                    startActivity(Intent(this, ExpenseActivity::class.java).apply { putExtra("userId", userId) })
                }
                R.id.nav_spending -> {
                    startActivity(Intent(this, ViewExpensesActivity::class.java).apply { putExtra("userId", userId) })
                }
                R.id.nav_budgets -> {
                    startActivity(Intent(this, CategoryTotalsActivity::class.java).apply { putExtra("userId", userId) })
                }
                R.id.nav_goals -> {
                    startActivity(Intent(this, SetGoalsActivity::class.java).apply { putExtra("userId", userId) })
                }
                R.id.nav_category -> {
                    startActivity(Intent(this, CategoryActivity::class.java).apply { putExtra("userId", userId) })
                }
                R.id.nav_stats -> {
                    // TODO: Navigate to StatsActivity when created
                }
            }
            drawerLayout?.closeDrawer(GravityCompat.START)
            true
        }

        // 6. Database Switcher and Data Migration (Firebase)
        val switchDatabaseMode = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchDatabaseMode)
        val btnMigrateData = findViewById<android.widget.Button>(R.id.btnMigrateData)

        // Set initial state of switch from preferences
        switchDatabaseMode?.isChecked = FirebaseHelper.isFirebaseEnabled(this)

        switchDatabaseMode?.setOnCheckedChangeListener { _, isChecked ->
            FirebaseHelper.setDatabaseMode(this, isChecked)
            val modeText = if (isChecked) "Online Firebase Database" else "Local Room Database"
            android.widget.Toast.makeText(this, "Switched to $modeText", android.widget.Toast.LENGTH_SHORT).show()
        }

        btnMigrateData?.setOnClickListener {
            // Disable button during migration to prevent double-clicks
            btnMigrateData.isEnabled = false
            btnMigrateData.text = "Migrating data..."
            
            lifecycleScope.launch {
                val success = FirebaseHelper.migrateRoomToFirebase(applicationContext, userId)
                runOnUiThread {
                    btnMigrateData.isEnabled = true
                    btnMigrateData.text = "Migrate Local Data to Firebase"
                    if (success) {
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "Migration successful! Data is stored in Firebase Realtime Database.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } else {
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "Migration failed! Please check your network connection and Firebase rules.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}