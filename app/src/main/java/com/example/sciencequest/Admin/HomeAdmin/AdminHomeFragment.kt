package com.example.sciencequest.Admin.HomeAdmin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sciencequest.R
import com.example.sciencequest.databinding.FragmentAdminHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference

        fetchTotalUsers()
        fetchActiveUser()
        fetchTotalCourses()
        fetchPopularCourse()
        fetchUserName()
        setupNavigation()

        return binding.root
    }

    private fun fetchTotalUsers() {
        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalUsers = snapshot.childrenCount.toInt()
                binding.totalUsersTextView.text = "Total Users: $totalUsers"
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to fetch users: ${error.message}")
            }
        })
    }

    private fun fetchUserName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child("Users").child(userId).child("username")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.getValue(String::class.java) ?: "Unknown User"
                    binding.userName.text = userName // Update UI
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Failed to fetch username: ${error.message}")
                }
            })
    }


    private fun fetchActiveUser() {
        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var topUser: String? = null
                var highestScore = 0

                for (user in snapshot.children) {
                    val username = user.child("username").value?.toString() ?: "Unknown"
                    val score = user.child("totalScore").getValue(Int::class.java) ?: 0

                    if (score > highestScore) {
                        highestScore = score
                        topUser = username
                    }
                }
                binding.activeUserTextView.text = "Most Active User: $topUser ($highestScore pts)"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to fetch active user: ${error.message}")
            }
        })
    }

    private fun fetchTotalCourses() {
        database.child("Lessons").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalCourses = snapshot.childrenCount.toInt()
                binding.totalCoursesTextView.text = "Total Courses: $totalCourses"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to fetch courses: ${error.message}")
            }
        })
    }

    private fun fetchPopularCourse() {
        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courseScores = mutableMapOf<String, Int>()

                for (user in snapshot.children) {
                    val categoryTotalScore = user.child("categoryTotalScore")
                    for (course in categoryTotalScore.children) {
                        val courseName = course.key ?: "Unknown"
                        val score = course.getValue(Int::class.java) ?: 0
                        courseScores[courseName] = courseScores.getOrDefault(courseName, 0) + score
                    }
                }

                val popularCourse = courseScores.maxByOrNull { it.value }?.key ?: "No Data"
                binding.popularCourseTextView.text = "Popular Course: $popularCourse"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to fetch popular course: ${error.message}")
            }
        })
    }

    private fun setupNavigation() {
        binding.btnCreateQuiz.setOnClickListener {
            findNavController().navigate(R.id.adminQuizDetailFragment)
        }

        binding.btnCreateLesson.setOnClickListener {
            findNavController().navigate(R.id.fragment_add_topic)
        }

        binding.btnEditTopics.setOnClickListener {
            findNavController().navigate(R.id.editTopicFragment)
        }

        binding.btnEditLessons.setOnClickListener {
            findNavController().navigate(R.id.adminLessonListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
