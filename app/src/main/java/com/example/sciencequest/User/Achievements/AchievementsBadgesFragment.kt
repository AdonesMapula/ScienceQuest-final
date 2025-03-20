package com.example.sciencequest.User.Achievements

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AchievementsBadgesFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var progressBarBiology: ProgressBar
    private lateinit var progressBarChemistry: ProgressBar
    private lateinit var progressBarPhysics: ProgressBar
    private lateinit var progressBarMath: ProgressBar
    private lateinit var progressBarEarth: ProgressBar

    private lateinit var badgeTextView: TextView
    private lateinit var userId: String // Get this from FirebaseAuth or passed argument
    private lateinit var userEmail: String // Get this from FirebaseAuth or passed argument
    private lateinit var userDisplayName: String // Get this from FirebaseAuth or passed argument
    private lateinit var userRole: String // Get this from FirebaseAuth or passed argument

    private lateinit var statusBiology: TextView
    private lateinit var statusChemistry: TextView
    private lateinit var statusPhysics: TextView
    private lateinit var statusMath: TextView
    private lateinit var statusEarth: TextView

    private lateinit var percentBiology: TextView
    private lateinit var percentChemistry: TextView
    private lateinit var percentPhysics: TextView
    private lateinit var percentMath: TextView
    private lateinit var percentEarth: TextView

    private lateinit var layoutBiology: ConstraintLayout
    private lateinit var layoutChemistry: ConstraintLayout
    private lateinit var layoutPhysics: ConstraintLayout
    private lateinit var layoutMath: ConstraintLayout
    private lateinit var layoutEarth: ConstraintLayout

    private lateinit var ribbonBiology: ImageView
    private lateinit var ribbonChemistry: ImageView
    private lateinit var ribbonPhysics: ImageView
    private lateinit var ribbonMath: ImageView
    private lateinit var ribbonEarth: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements_badges, container, false)

        val sharedPreferences =
            requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null).toString()
        userEmail = sharedPreferences.getString("userEmail", null).toString()
        userDisplayName = sharedPreferences.getString("userDisplayName", null).toString()
        userRole = sharedPreferences.getString("userRole", null).toString()

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance().getReference("Users")

        // UI Elements
        progressBarBiology = view.findViewById(R.id.progressBarBiology)
        progressBarChemistry = view.findViewById(R.id.progressBarChemistry)
        progressBarPhysics = view.findViewById(R.id.progressBarPhysics)
        progressBarMath = view.findViewById(R.id.progressBarMath)
        progressBarEarth = view.findViewById(R.id.progressBarEarth)

        statusBiology = view.findViewById(R.id.statusBiology)
        statusChemistry = view.findViewById(R.id.statusChemistry)
        statusPhysics = view.findViewById(R.id.statusPhysics)
        statusMath = view.findViewById(R.id.statusMath)
        statusEarth = view.findViewById(R.id.statusEarth)

        percentBiology = view.findViewById(R.id.percentBiology)
        percentChemistry = view.findViewById(R.id.percentChemistry)
        percentPhysics = view.findViewById(R.id.percentPhysics)
        percentMath = view.findViewById(R.id.percentMath)
        percentEarth = view.findViewById(R.id.percentEarth)

        layoutBiology = view.findViewById(R.id.layoutBiology)
        layoutChemistry = view.findViewById(R.id.layoutChemistry)
        layoutPhysics = view.findViewById(R.id.layoutPhysics)
        layoutMath = view.findViewById(R.id.layoutMath)
        layoutEarth = view.findViewById(R.id.layoutEarth)

        ribbonBiology = view.findViewById(R.id.ribbonBiology)
        ribbonChemistry = view.findViewById(R.id.ribbonChemistry)
        ribbonPhysics = view.findViewById(R.id.ribbonPhysics)
        ribbonMath = view.findViewById(R.id.ribbonMath)
        ribbonEarth = view.findViewById(R.id.ribbonEarth)

        // badgeTextView = view.findViewById(R.id.badgeTextView)

        // Fetch achievements & badges
        fetchAchievementProgress(userId)

        val backBtn = view.findViewById<ImageButton>(R.id.backBtn)
        backBtn.setOnClickListener { findNavController().popBackStack() }

        return view
    }

    private fun fetchAchievementProgress(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val userProgressRef = database.getReference("Users").child(userId).child("lessonProgress")
        val lessonsRef = database.getReference("Lessons")
        val streakRef = database.getReference("Users").child(userId).child("streak") // Fetch streak directly

        Log.d("Firebase", "Fetching achievement progress for user: $userId")

        userProgressRef.get().addOnSuccessListener { userSnapshot ->
            Log.d("Firebase", "User progress data retrieved successfully")

            lessonsRef.get().addOnSuccessListener { lessonsSnapshot ->
                Log.d("Firebase", "Lessons data retrieved successfully")

                streakRef.get().addOnSuccessListener { streakSnapshot ->
                    Log.d("Firebase", "Streak data retrieved successfully")

                    val achievements = mutableMapOf<String, Int>() // Stores completed lessons
                    val totalLessonsMap = mutableMapOf<String, Int>()
                    var totalQuizzes = 0
                    var totalScore = 0
                    val streak = streakSnapshot.getValue(Int::class.java) ?: 0 // Get streak from Firebase

                    Log.d("Firebase", "User streak: $streak")

                    lessonsSnapshot.children.forEach { branchSnapshot ->
                        val branchName = branchSnapshot.key ?: return@forEach

                        // Skip branch-level metadata fields
                        if (branchName in listOf("fieldDescription", "fieldImageUrl", "fieldName")) {
                            Log.d("Firebase", "Skipping metadata field: $branchName")
                            return@forEach
                        }

                        val totalLessons = branchSnapshot.children.sumOf { topicSnapshot ->
                            if (topicSnapshot.key in listOf("fieldDescription", "fieldImageUrl", "fieldName")) 0
                            else topicSnapshot.children.count { it.key != "topicDesc" } // ✅ Count only lessons, exclude "topicDesc"
                        }
                        totalLessonsMap[branchName] = totalLessons


                        Log.d("Firebase", "Branch: $branchName, Total Lessons: $totalLessons")

                        var completedLessons = 0

                        userSnapshot.child(branchName).children.forEach { topicSnapshot ->
                            val topicName = topicSnapshot.key ?: return@forEach

                            // Skip topic-level metadata field
                            if (topicName == "topicDesc") {
                                Log.d("Firebase", "Skipping topic description: $topicName")
                                return@forEach
                            }

                            topicSnapshot.children.forEach { lessonSnapshot ->
                                val lessonName = lessonSnapshot.key ?: return@forEach

                                // Skip unwanted lesson fields
                                if (lessonName == "topicDesc") {
                                    Log.d("Firebase", "Skipping topic description at lesson level: $lessonName")
                                    return@forEach
                                }

                                val quizCompleted = lessonSnapshot.child("quizStatus").value == "COMPLETED"
                                val simCompleted = lessonSnapshot.child("simulationStatus").value == "COMPLETED"
                                val videoCompleted = lessonSnapshot.child("videoStatus").value == "COMPLETED"

                                Log.d(
                                    "Firebase",
                                    "Lesson: $lessonName, Quiz: $quizCompleted, Sim: $simCompleted, Video: $videoCompleted"
                                )

                                if (quizCompleted) {
                                    totalQuizzes++ // Count completed quizzes
                                    val score = lessonSnapshot.child("quiz").child("quizScore").getValue(Int::class.java) ?: 0
                                    totalScore += score // Sum up the scores
                                    Log.d("Firebase", "Quiz completed. Score: $score, Total Score: $totalScore")
                                }

                                if (quizCompleted && simCompleted && videoCompleted) {
                                    completedLessons++ // ✅ Count only fully completed lessons
                                    Log.d("Firebase", "Lesson '$lessonName' marked as completed")
                                }
                            }
                        }

                        achievements[branchName] = completedLessons // ✅ Store completed lessons
                        Log.d("Firebase", "Completed lessons in $branchName: $completedLessons")
                    }

                    // Log final computed values before passing to UI
                    Log.d("Firebase", "Total Quizzes: $totalQuizzes, Total Score: $totalScore")
                    Log.d("Firebase", "Achievements Map: $achievements")
                    Log.d("Firebase", "Total Lessons Map: $totalLessonsMap")

                    // Pass calculated values to updateUI
                    updateUI(achievements, totalLessonsMap, totalQuizzes, totalScore, streak)
                }.addOnFailureListener {
                    Log.e("Firebase", "Error fetching streak data: ${it.message}")
                }
            }.addOnFailureListener {
                Log.e("Firebase", "Error fetching lessons data: ${it.message}")
            }
        }.addOnFailureListener {
            Log.e("Firebase", "Error fetching user progress data: ${it.message}")
        }
    }



    private fun updateUI(
        achievements: Map<String, Int>,
        totalLessonsMap: Map<String, Int>,
        totalQuizzes: Int,
        totalScore: Int,
        streak: Int
    ) {
        achievements.forEach { (category, completedLessons) ->
            val totalLessons = totalLessonsMap[category] ?: 1
            val progress = (completedLessons.toDouble() / totalLessons) * 100
            val isComplete = progress.toInt() == 100
            val statusText = if (isComplete) "Complete" else "Incomplete"

            val progressBar: ProgressBar?
            val percentText: TextView?
            val statusTextView: TextView?
            val container: View?
            val icon: ImageView?

            when (category) {
                "Biology" -> {
                    progressBar = progressBarBiology
                    percentText = percentBiology
                    statusTextView = statusBiology
                    container = layoutBiology
                    icon = ribbonBiology
                }
                "Chemistry" -> {
                    progressBar = progressBarChemistry
                    percentText = percentChemistry
                    statusTextView = statusChemistry
                    container = layoutChemistry
                    icon = ribbonChemistry
                }
                "Physics" -> {
                    progressBar = progressBarPhysics
                    percentText = percentPhysics
                    statusTextView = statusPhysics
                    container = layoutPhysics
                    icon = ribbonPhysics
                }
                "Math" -> {
                    progressBar = progressBarMath
                    percentText = percentMath
                    statusTextView = statusMath
                    container = layoutMath
                    icon = ribbonMath
                }
                "Earth" -> {
                    progressBar = progressBarEarth
                    percentText = percentEarth
                    statusTextView = statusEarth
                    container = layoutEarth
                    icon = ribbonEarth
                }
                else -> return@forEach
            }

            progressBar?.progress = progress.toInt()
            percentText?.text = "${progress.toInt()}%"
            statusTextView?.text = statusText

            container?.setBackgroundResource(if (isComplete) R.drawable.bio_item_bg else R.drawable.disabled_item_bg)
            icon?.setImageResource(if (isComplete) R.drawable.ribbon_img else R.drawable.disabled_ribbon_img)
        }

        updateBadges(totalQuizzes, achievements, totalScore, streak)
    }


    private fun updateBadges(totalQuizzes: Int, achievements: Map<String, Int>, totalScore: Int, streak: Int) {
        val badgeList = mutableListOf<Badge>()

        if (totalQuizzes >= 10) badgeList.add(Badge(R.drawable.ribbon_img, "Quiz Master"))
        if (achievements.values.sum() >= 5) badgeList.add(Badge(R.drawable.ribbon_img, "Module Pro"))
        if (totalScore >= 50) badgeList.add(Badge(R.drawable.ribbon_img, "Score Champ"))
        if (streak >= 5) badgeList.add(Badge(R.drawable.ribbon_img, "Streak Star"))

        val recentBadges = badgeList.takeLast(3)

        val recyclerView: RecyclerView = view?.findViewById(R.id.badgeRecyclerView) ?: return
        val emptyTextView: TextView = view?.findViewById(R.id.emptyBadgeText) ?: return

        if (recentBadges.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = BadgeAdapter(recentBadges)
        }
    }

}