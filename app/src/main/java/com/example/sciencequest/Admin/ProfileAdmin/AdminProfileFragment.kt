package com.example.sciencequest.Admin.ProfileAdmin

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.sciencequest.R
import com.example.sciencequest.databinding.FragmentAdminProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminProfileFragment : Fragment() {
    private lateinit var binding: FragmentAdminProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Users")
        userId = auth.currentUser?.uid
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminProfileBinding.inflate(inflater, container, false)

        if (userId != null) {
            loadUserData()
        }

        binding.signOutBtn.setOnClickListener {
            val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            auth.signOut()

            findNavController().navigate(R.id.loginFragment)
        }

        // Handle Edit Icon Click
        binding.editIcon.setOnClickListener {
            findNavController().navigate(R.id.adminProfileEditFragment)
        }

        return binding.root
    }

    private fun loadUserData() {
        userId?.let { uid ->
            database.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").getValue(String::class.java) ?: "Unknown"
                        val email = snapshot.child("email").getValue(String::class.java) ?: "No email"
                        val avatarUrl = snapshot.child("avatar").getValue(String::class.java) ?: ""

                        binding.userName.text = username
                        binding.userEmail.text = email

                        if (avatarUrl.isNotEmpty()) {
                            Glide.with(requireContext())
                                .load(avatarUrl)
                                .placeholder(R.drawable.male1)
                                .into(binding.profileImage)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load profile!", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
