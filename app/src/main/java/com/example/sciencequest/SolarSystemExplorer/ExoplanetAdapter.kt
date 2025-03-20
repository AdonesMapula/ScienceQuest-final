package com.example.sciencequest.SolarSystemExplorer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class ExoplanetAdapter : RecyclerView.Adapter<ExoplanetAdapter.ExoplanetViewHolder>() {

    private var exoplanets = listOf<Exoplanet>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExoplanetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exoplanet, parent, false)
        return ExoplanetViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExoplanetViewHolder, position: Int) {
        val exoplanet = exoplanets[position]
        holder.bind(exoplanet)
    }

    override fun getItemCount() = exoplanets.size

    fun submitList(list: List<Exoplanet>) {
        exoplanets = list
        notifyDataSetChanged()
    }

    class ExoplanetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.exoplanet_name)

        fun bind(exoplanet: Exoplanet) {
            nameTextView.text = exoplanet.name
        }
    }
}
