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
import com.example.prog7313appupdated.database.AppDatabase
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

        // UI will be loaded in onResume so it updates when coming back from other screens
    }

    override fun onResume() {
        super.onResume()
        if (userId != -1) {
            refreshDashboardData()
        }
    }

    private fun refreshDashboardData() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val user = db.userDao().getUserById(userId)

                user?.let {
                    // Update Welcome text
                    val displayName = if (it.firstName.isNotEmpty()) it.firstName else it.username
                    findViewById<TextView>(R.id.tvWelcome)?.text = "Hello, $displayName! 👋"

                    // Update Budget UI
                    val maxGoal = it.maxBudgetGoal
                    findViewById<TextView>(R.id.tvMonthlyBudgetGoal)?.text = "OF R${maxGoal.toInt()}"

                    // Note: Mocking spent expenses for now until date queries are fully implemented
                    val totalSpent = 0.0
                    findViewById<TextView>(R.id.tvMonthlyBudget)?.text = "R${totalSpent.toInt()}"
                    
                    val remaining = maxGoal - totalSpent
                    findViewById<TextView>(R.id.tvMonthText)?.text = "You have R${remaining.toInt()} left for the month."

                } ?: run {
                    findViewById<TextView>(R.id.tvWelcome)?.text = "Hello! 👋"
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Database error: ${e.message}")
            }
        }

        // 4. Floating Action Button
        findViewById<FloatingActionButton>(R.id.btnAddExpense)?.setOnClickListener {
            val intent = Intent(this, ExpenseActivity::class.java)
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
                R.id.nav_stats -> {
                    // TODO: Navigate to StatsActivity when created
                }
                R.id.nav_logout -> {
                    // Clear the remember me state
                    val sharedPreferences = getSharedPreferences("MoneyGoalsPrefs", android.content.Context.MODE_PRIVATE)
                    sharedPreferences.edit().clear().apply()

                    // Return to login screen and clear activity stack
                    val logoutIntent = Intent(this, LoginActivity::class.java)
                    logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(logoutIntent)
                    finish()
                }
            }
            drawerLayout?.closeDrawer(GravityCompat.START)
            true
        }
    }
}