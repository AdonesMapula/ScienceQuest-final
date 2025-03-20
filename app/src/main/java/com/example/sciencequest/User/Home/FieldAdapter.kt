package com.example.sciencequest.User.Home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sciencequest.R

class FieldAdapter (
    private val fieldList: List<Field>,
    private val onFieldClick: (String) -> Unit
) : RecyclerView.Adapter<FieldAdapter.FieldViewHolder>(){

    inner class FieldViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fieldName: TextView = view.findViewById(R.id.fieldTv)
        val fieldImg : ImageView = view.findViewById(R.id.fieldImg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldViewHolder {
        // Inflate the layout for each item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_field, parent, false)
        return FieldViewHolder(view)
    }

    override fun onBindViewHolder(holder: FieldViewHolder, position: Int) {
        // Bind the field name and image to the view
        val field = fieldList[position]
        holder.fieldName.text = field.fieldName


        // Using Glide or Picasso to load the image from URL
        Glide.with(holder.itemView.context)
            .load(field.fieldImageUrl)
            .into(holder.fieldImg)

        holder.itemView.setOnClickListener { onFieldClick(field.fieldName) }
    }

    override fun getItemCount(): Int {
        return fieldList.size
    }
}

