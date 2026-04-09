package com.example.prog7313appupdated.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313appupdated.database.entities.Achievement
import com.example.prog7313appupdated.R

class AchievementAdapter(private val items: List<Achievement>) :
    RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivAchievementIcon)
        val tvName: TextView = view.findViewById(R.id.tvAchievementName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.title
        holder.ivIcon.setImageResource(item.iconRes)
    }

    override fun getItemCount() = items.size
}