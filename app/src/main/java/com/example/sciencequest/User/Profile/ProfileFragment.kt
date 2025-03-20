package com.example.sciencequest.User.Profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.sciencequest.R
import com.example.sciencequest.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null

    private val navOptions = NavOptions.Builder()
        .setPopUpTo(R.id.nav_graph, true) // Clear back stack
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")
        userId = auth.currentUser?.uid
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        userId?.let {
            loadUserData()
        }

        binding.signOutBtn.setOnClickListener {
            signOutUser()
        }

        binding.viewAllBadgesBtn.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionUserProfileFragmentToAchievementsBadgesFragment())
        }

        binding.editProfileBtn.setOnClickListener {
            findNavController().navigate(R.id.profileEditFragment)
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
                        val avatar = snapshot.child("avatar").getValue(String::class.java) ?: ""
                        val streak = snapshot.child("streak").getValue(Int::class.java) ?: 0
                        val totalScore = snapshot.child("totalScore").getValue(Int::class.java) ?: 0

                        binding.userName.text = username
                        binding.email.text = email
                        binding.tvStreakCount.text = streak.toString()
                        binding.tvPointsCount.text = totalScore.toString()

                        // Avatar Mapping (for predefined avatars stored in drawable)
                        val avatarMap = mapOf(
                            "avatar1" to R.drawable.female1,
                            "avatar2" to R.drawable.female2,
                            "avatar3" to R.drawable.male1,
                            "avatar4" to R.drawable.male2,
                            "avatar5" to R.drawable.female3,
                            "avatar6" to R.drawable.girl1,
                            "avatar7" to R.drawable.boy1,
                            "avatar8" to R.drawable.boy2
                        )

                        // Determine if avatar is a URL or predefined
                        if (avatar.startsWith("http")) {
                            // Load from URL using Glide
                            Glide.with(requireContext())
                                .load(avatar)
                                .placeholder(R.drawable.male1) // Default avatar if URL fails
                                .into(binding.userProfile2)
                        } else {
                            // Load predefined avatar from drawable
                            val avatarResId = avatarMap[avatar] ?: R.drawable.male1
                            binding.userProfile2.setImageResource(avatarResId)
                        }

                        // Badge logic
                        val badgeCount = countCompletedQuizzes(snapshot.child("lessonProgress"))
                        binding.tvBadgesCount.text = badgeCount.toString()
                        displayBadges(badgeCount)

                        // Determine rank
                        determineRank(totalScore)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load profile!", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun countCompletedQuizzes(lessonProgressSnapshot: DataSnapshot): Int {
        var completedCount = 0
        for (category in lessonProgressSnapshot.children) {
            for (lesson in category.children) {
                val quizStatus = lesson.child("quizStatus").getValue(String::class.java)
                if (quizStatus == "COMPLETED") {
                    completedCount++
                }
            }
        }
        return completedCount
    }

    private fun displayBadges(badgeCount: Int) {
        if (badgeCount == 0) {
            binding.badgeContainer1.setImageResource(R.drawable.disabled_ribbon_img)
            binding.badgeContainer2.setImageResource(R.drawable.disabled_ribbon_img)
            binding.badgeContainer3.setImageResource(R.drawable.disabled_ribbon_img)
        } else {
            val badgeImages = listOf(binding.badgeContainer1, binding.badgeContainer2, binding.badgeContainer3)
            for (i in badgeImages.indices) {
                if (i < badgeCount) {
                    badgeImages[i].setImageResource(R.drawable.ribbon_img)
                } else {
                    badgeImages[i].setImageResource(R.drawable.disabled_ribbon_img)
                }
            }
        }
    }

    private fun determineRank(userScore: Int) {
        database.orderByChild("totalScore").limitToLast(10).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val topScores = snapshot.children.mapNotNull { it.child("totalScore").getValue(Int::class.java) }.sortedDescending()
                val minTopScore = topScores.lastOrNull() ?: 0
                binding.tvCurrentRank.text = if (userScore >= minTopScore) "Ranked" else "Unranked"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Error fetching ranks", error.toException())
            }
        })
    }

    private fun signOutUser() {
        val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        auth.signOut()

        findNavController().navigate(R.id.loginFragment, null, navOptions)
        Log.d("ProfileFragment", "User signed out")
    }
}