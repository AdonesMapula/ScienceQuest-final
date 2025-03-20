package com.example.sciencequest.User.LessonLibrary

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LessonListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var lessonAdapter: LessonAdapter
    private val lessons = mutableListOf<Lesson>()
    private lateinit var progressBar: ProgressBar
    private var topic: String? = null
    private var category: String? = null
    private lateinit var backButton: ImageButton
    private var userId: String? = null

    private val categoryConfig = mapOf(
        "Physics" to CategoryConfig(
            fragmentBackgroundColor = R.color.darkBlueButton,
            itemBackgroundColor = R.color.darkBlueButton,
            fragmentImage = R.drawable.physics_category_icon
        ),
        "Chemistry" to CategoryConfig(
            fragmentBackgroundColor = R.color.greenButton,
            itemBackgroundColor = R.color.greenButton,
            fragmentImage = R.drawable.chemistry_category_icon
        ),
        "Biology" to CategoryConfig(
            fragmentBackgroundColor = R.color.blueButton,
            itemBackgroundColor = R.color.blueButton,
            fragmentImage = R.drawable.biology_category_icon
        ),
        "Math" to CategoryConfig(
            fragmentBackgroundColor = R.color.reddishPinkButton,
            itemBackgroundColor = R.color.reddishPinkButton,
            fragmentImage = R.drawable.math_category_icon
        ),
        "Earth" to CategoryConfig(
            fragmentBackgroundColor = R.color.pinkButton,
            itemBackgroundColor = R.color.pinkButton,
            fragmentImage = R.drawable.earth_category_icon
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lesson_list, container, false)

        val sharedPreferences =
            requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)


        topic = arguments?.getString("topic")
        category = arguments?.getString("category")?.trim() ?: "default_category"  // Ensure it's always initialized





        Log.d("ListTopic", "$category $topic")

        // Get the configuration for the current category
        val config = categoryConfig[category] ?: CategoryConfig(
            fragmentBackgroundColor = R.color.greenerBg,
            itemBackgroundColor = R.color.greenerBg,
            fragmentImage = R.drawable.biology_category_icon
        )

        val bannerBg = view.findViewById<LinearLayout>(R.id.bannerBg)
        // Set fragment background color
        bannerBg.setBackgroundColor(requireContext().getColor(config.fragmentBackgroundColor))


        // Set the image in the fragment
//        val fieldImage = view.findViewById<ImageView>(R.id.fieldImg)
//        fieldImage.setImageResource(config.fragmentImage)

        // Initialize RecyclerView and ProgressBar
        recyclerView = view.findViewById(R.id.lessonListRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        // Set up the adapter for displaying lessons
        lessonAdapter = LessonAdapter(lessons, config.itemBackgroundColor) { lesson ->
            val action = LessonListFragmentDirections
                .actionLessonListFragmentToLessonDetailFragment(lesson.id, category!!, topic!!)
            findNavController().navigate(action)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = lessonAdapter

        // Show the ProgressBar while loading
        progressBar.visibility = View.VISIBLE
        val topicDescTv = view.findViewById<TextView>(R.id.topicDescTv)

        // Load all lessons from Firebase
        loadAllLessonsFromFirebase { loadedLessons, fieldDescription, topicProgress, topicDesc  ->

            lessons.clear()
            lessons.addAll(loadedLessons)
            lessonAdapter.notifyDataSetChanged()

            topicDescTv.text = topicDesc // Set the topic description in the TextView

            Log.d("Firebase", "Topic Progress: $topicProgress")

            val topicTv: TextView = view.findViewById(R.id.topicTv)
            topicTv.text = "$topic"

            // Update the progress bar for the topic
//            val topicProgressBar: ProgressBar = view.findViewById(R.id.topicProgressBar)
//            topicProgressBar.progress = topicProgress
            val topicProgressPercentage: TextView = view.findViewById(R.id.topicProgressPercentage)
            topicProgressPercentage.text = "$topicProgress%"

            // Hide the ProgressBar after loading
            progressBar.visibility = View.GONE
        }

        backButton = view.findViewById(R.id.backBtn)
        backButton.setOnClickListener{
            findNavController().popBackStack()
        }


        return view
    }


    private fun loadAllLessonsFromFirebase(callback: (List<Lesson>, String, Int, String) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val lessons = mutableListOf<Lesson>()
        var totalProgress = 0
        var lessonCount = 0

        // First, load all lesson details (title, description, etc.) from the Lessons node
        val lessonsRef = database.getReference("Lessons")
            .child(category.toString())   // Navigate to category
            .child(topic.toString())      // Navigate to topic within the category

        lessonsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(lessonDetailsSnapshot: DataSnapshot) {
                var topicDesc = "No description available" // Default value for topicDesc

                // Get topicDesc if available
                if (lessonDetailsSnapshot.child("topicDesc").exists()) {
                    topicDesc = lessonDetailsSnapshot.child("topicDesc").getValue(String::class.java) ?: "No description available"
                }

                // Iterate through all lessons and add them to the list
                for (lessonSnapshot in lessonDetailsSnapshot.children) {
                    if (lessonSnapshot.key == "topicDesc") continue // Skip topicDesc node

                    val lessonId = lessonSnapshot.key ?: continue
                    val title = lessonSnapshot.child("title").getValue(String::class.java) ?: "Untitled"
                    val description = lessonSnapshot.child("description").getValue(String::class.java) ?: "No description"

                    // Add lesson to the list with a default progress of 0
                    lessons.add(Lesson(lessonId, title, description, 0))

                    // Increase the lesson count
                    lessonCount++
                }

                // Fetch user progress data
                val userProgressRef = database.getReference("Users").child(userId!!).child("lessonProgress")
                    .child(category.toString()).child(topic.toString())  // Accessing category and topic

                userProgressRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userProgressSnapshot: DataSnapshot) {
                        val userProgressMap = mutableMapOf<String, Int>()

                        // Iterate over user progress and update the progress of each lesson
                        for (lessonSnapshot in userProgressSnapshot.children) {
                            val lessonId = lessonSnapshot.key ?: continue
                            val quizStatus = lessonSnapshot.child("quizStatus").getValue(String::class.java) ?: "UNTOUCHED"
                            val videoStatus = lessonSnapshot.child("videoStatus").getValue(String::class.java) ?: "UNTOUCHED"
                            val simulationStatus = lessonSnapshot.child("simulationStatus").getValue(String::class.java) ?: "UNTOUCHED"

                            // Calculate progress based on statuses
//                            val progress = when {
//                                quizStatus == "COMPLETED" -> 25
//                                videoStatus == "COMPLETED" -> 25
//                                simulationStatus == "COMPLETED" -> 25
//                                else -> 0
//                            }

                            val progress = when {
                                quizStatus == "COMPLETED" && videoStatus == "COMPLETED" && simulationStatus == "COMPLETED" -> 100
                                else -> (if (quizStatus == "COMPLETED") 33 else 0) +
                                        (if (videoStatus == "COMPLETED") 33 else 0) +
                                        (if (simulationStatus == "COMPLETED") 33 else 0)
                            }


                            // Update progress map for this lesson
                            userProgressMap[lessonId] = progress
                        }

                        // Now, update the progress for each lesson
                        for (lesson in lessons) {
                            val progress = userProgressMap[lesson.id] ?: 0
                            lesson.progress = progress

                            // Calculate total progress after all lessons are added
                            totalProgress += progress
                        }

                        // Calculate average progress
                        val averageProgress = if (lessonCount > 0) totalProgress / lessonCount else 0

                        // Fetch the fieldDescription for the category
                        database.getReference("Lessons").child(category.toString()).child("fieldDescription")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(fieldDescriptionSnapshot: DataSnapshot) {
                                    val fieldDescription = fieldDescriptionSnapshot.getValue(String::class.java)
                                        ?: "No description available"

                                    // Return the loaded lessons, fieldDescription, and the user's progress for the topic
                                    callback(lessons, fieldDescription, averageProgress, topicDesc)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Error loading field description", error.toException())
                                }
                            })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error loading user progress", error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading lessons", error.toException())
            }
        })
    }









}
