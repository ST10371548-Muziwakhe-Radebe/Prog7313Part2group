package com.example.prog7313appupdated.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313appupdated.R
import com.example.prog7313appupdated.database.entities.CategoryTotal

class CategoryTotalAdapter(
    private var categoryTotals: List<CategoryTotal>,
    private var categoryMap: Map<Int, String>
) : RecyclerView.Adapter<CategoryTotalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvCategoryTotal: TextView = view.findViewById(R.id.tvCategoryTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoryTotal = categoryTotals[position]
        
        // Get category name or fallback to ID
        val categoryName = categoryMap[categoryTotal.categoryId] ?: "Unknown Category ${categoryTotal.categoryId}"
        holder.tvCategoryName.text = categoryName
        
        // Format amount
        holder.tvCategoryTotal.text = String.format("R%.2f", categoryTotal.total)
    }

    override fun getItemCount() = categoryTotals.size

    fun updateData(newTotals: List<CategoryTotal>, newMap: Map<Int, String>) {
        categoryTotals = newTotals
        categoryMap = newMap
        notifyDataSetChanged()
    }
}
