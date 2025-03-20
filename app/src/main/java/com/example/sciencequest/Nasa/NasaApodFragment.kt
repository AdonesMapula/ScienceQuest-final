package com.example.sciencequest.Nasa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.sciencequest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NasaApodFragment : Fragment() {

    private lateinit var titleTextView: TextView
    private lateinit var explanationTextView: TextView
    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nasa_apod, container, false)

        // Initialize views
        titleTextView = view.findViewById(R.id.apodTitle)
        explanationTextView = view.findViewById(R.id.apodExplanation)
        imageView = view.findViewById(R.id.apodImage)

        // Fetch data from API
        fetchApod()

        return view
    }

    private fun fetchApod() {
        val apiKey = "HKNGERJgUdtSlNIh8rLycGqsBYi73QFflb1VbHJX"

        // Make network call using Coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.nasaApiService.getApod(apiKey)

                // Update UI on Main thread
                withContext(Dispatchers.Main) {
                    titleTextView.text = response.title
                    explanationTextView.text = response.explanation
                    Glide.with(requireContext())
                        .load(response.url)
                        .into(imageView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
