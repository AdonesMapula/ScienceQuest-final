package com.example.sciencequest.Nasa

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.sciencequest.databinding.FragmentEarthImagesBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response


class EarthImagesFragment : Fragment() {

    private lateinit var binding: FragmentEarthImagesBinding
    private lateinit var adapter: EarthImagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEarthImagesBinding.inflate(inflater, container, false)

        // Fetch Earth images
        fetchEarthImages()

        return binding.root
    }

    private fun fetchEarthImages() {
        val apiKey = "oLA6M2Od2akfv4GVc2dTaZbd8pJdJysrqAmVrMrQ" // Use your actual NASA API key

        lifecycleScope.launch {
            try {
                val apiUrl = "https://api.nasa.gov/EPIC/api/natural/images?api_key=$apiKey"
                Log.d("EarthImageFragment", "Requesting from URL: $apiUrl")


                val response = RetrofitClient.nasaApiService.getEarthImages(apiKey)

                // Check if the response is successful
                if (response.isNotEmpty()) {
                    adapter = EarthImagesAdapter(response)
                    binding.recyclerView.adapter = adapter
                } else {
                    Log.e("EarthImageFragment", "No images found")
                }

            } catch (e: HttpException) {
                Log.e("EarthImageFragment", "Error fetching images: ${e.message()}", e)
                Log.e("EarthImageFragment", "Response code: ${e.code()}")
            } catch (e: Exception) {
                Log.e("EarthImageFragment", "Unexpected error", e)
            }
        }
    }

}