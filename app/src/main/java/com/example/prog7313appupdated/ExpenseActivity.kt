package com.example.prog7313appupdated

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExpenseActivity : AppCompatActivity() {

    data class Transaction(
        val title: String,
        val subtitle: String,
        val amount: String,
        val color: String,
        val iconRes: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        val transactions = listOf(
            Transaction("Supermarket", "Groceries • Oct 28", "-R450.00", "#4A90E2", android.R.drawable.ic_menu_agenda),
            Transaction("Gas Station", "Transport • Oct 25", "-R800.00", "#F5A623", android.R.drawable.ic_menu_compass),
            Transaction("Coffee Shop", "Food • Oct 22", "-R65.50", "#BD10E0", android.R.drawable.ic_menu_crop),
            Transaction("Cinema Plus", "Entertainment • Oct 18", "-R120.00", "#FF4081", android.R.drawable.ic_menu_camera),
            Transaction("Pharmacy", "Health • Oct 15", "-R130.00", "#00BCD4", android.R.drawable.ic_menu_add)
        )

        val rv = findViewById<RecyclerView>(R.id.rvTransactions)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tx = transactions[position]
                holder.itemView.findViewById<TextView>(R.id.tvTransactionTitle).text = tx.title
                holder.itemView.findViewById<TextView>(R.id.tvTransactionSubtitle).text = tx.subtitle
                holder.itemView.findViewById<TextView>(R.id.tvTransactionAmount).text = tx.amount

                val ivIcon = holder.itemView.findViewById<ImageView>(R.id.ivTransactionIcon)
                ivIcon.setImageResource(tx.iconRes)
                // Use a generic circle background with a custom tint based on design colors
                ivIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(tx.color))
            }

            override fun getItemCount() = transactions.size
        }

        findViewById<FloatingActionButton>(R.id.btnAddExpense)?.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
    }
}