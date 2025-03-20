package com.example.sciencequest.User.Achievements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R
import kotlin.random.Random

class BadgeAdapter(private val badges: List<Badge>) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    private val backgrounds = listOf(
        R.drawable.bio_item_bg, // Add your drawable backgrounds
        R.drawable.chem_item_bg,
        R.drawable.phys_item_bg,
        R.drawable.math_item_bg,
        R.drawable.earth_item_bg
    )

    class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val badgeIcon: ImageView = view.findViewById(R.id.badgeIcon)
        val badgeName: TextView = view.findViewById(R.id.badgeName)
        val badgeContainer: View = view.findViewById(R.id.badgeContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        holder.badgeIcon.setImageResource(badge.badgeIcon)
        holder.badgeName.text = badge.badgeName

        // Assign backgrounds in a cyclic manner
        val backgroundIndex = position % backgrounds.size
        holder.badgeContainer.setBackgroundResource(backgrounds[backgroundIndex])
    }

    override fun getItemCount(): Int = badges.size
}

