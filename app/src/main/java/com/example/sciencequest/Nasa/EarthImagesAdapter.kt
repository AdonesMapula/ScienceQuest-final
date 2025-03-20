package com.example.sciencequest.Nasa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sciencequest.databinding.EarthImageItemBinding
import com.github.chrisbanes.photoview.PhotoView

class EarthImagesAdapter(private val earthImages: List<EarthImage>) : RecyclerView.Adapter<EarthImagesAdapter.EarthImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarthImageViewHolder {
        val binding = EarthImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EarthImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EarthImageViewHolder, position: Int) {
        val earthImage = earthImages[position]
        val imageUrl = "https://epic.gsfc.nasa.gov/archive/natural/${earthImage.date.replace("-", "/")}/png/${earthImage.image}.png"

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.binding.photoView)

        // Set the date for reference (Optional)
        holder.binding.dateTextView.text = earthImage.date
    }

    override fun getItemCount() = earthImages.size

    class EarthImageViewHolder(val binding: EarthImageItemBinding) : RecyclerView.ViewHolder(binding.root)
}
