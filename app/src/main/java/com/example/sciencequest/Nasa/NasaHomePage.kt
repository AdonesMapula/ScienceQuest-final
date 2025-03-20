package com.example.sciencequest.Nasa

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sciencequest.R
import com.example.sciencequest.databinding.FragmentNasaHomePageBinding


class NasaHomePage : Fragment() {
    private lateinit var binding: FragmentNasaHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNasaHomePageBinding.inflate(inflater, container, false)

//        binding.spaceImageryExporationBtn.setOnClickListener{
//            val fragment = parentFragmentManager.beginTransaction()
//
//            fragment.replace(R.id.fragment_container, EarthImagesFragment()).addToBackStack(null).commit()
//        }
//
//        binding.nasaApodBtn.setOnClickListener{
//            val fragment = parentFragmentManager.beginTransaction()
//
//            fragment.replace(R.id.fragment_container, NasaApodFragment()).addToBackStack(null).commit()
//        }
//
//        binding.solarSystemExplorerBtn.setOnClickListener{
//            val fragment = parentFragmentManager.beginTransaction()
//
//            fragment.replace(R.id.fragment_container, NasaApodFragment()).addToBackStack(null).commit()
//        }




        // Inflate the layout for this fragment
        return binding.root




    }


}