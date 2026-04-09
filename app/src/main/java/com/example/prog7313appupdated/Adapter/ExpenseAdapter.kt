package com.example.prog7313appupdated.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313appupdated.R
import com.example.prog7313appupdated.database.entities.ExpenseEntry

class ExpenseAdapter(
    private var expenses: List<ExpenseEntry>,
    private val categoryMap: Map<Int, String>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    var onReceiptClick: ((String) -> Unit)? = null

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTransactionTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvTransactionSubtitle)
        val tvAmount: TextView = view.findViewById(R.id.tvTransactionAmount)
        val llReceiptTag: LinearLayout = view.findViewById(R.id.llReceiptTag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        val categoryName = categoryMap[expense.categoryId] ?: "Unknown Category"
        holder.tvTitle.text = categoryName

        // Subtitle: "Milk • 2026-10-30 • 14:00"
        holder.tvSubtitle.text = "${expense.description} • ${expense.date} • ${expense.startTime}"
        holder.tvAmount.text = "-R${expense.amount}"

        // Handle the optional Photo feature
        if (expense.photoPath != null) {
            holder.llReceiptTag.visibility = View.VISIBLE
            holder.llReceiptTag.setOnClickListener {
                onReceiptClick?.invoke(expense.photoPath)
            }
        } else {
            holder.llReceiptTag.visibility = View.GONE
            holder.llReceiptTag.setOnClickListener(null)
        }
    }

    override fun getItemCount() = expenses.size

    fun updateData(newExpenses: List<ExpenseEntry>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}
