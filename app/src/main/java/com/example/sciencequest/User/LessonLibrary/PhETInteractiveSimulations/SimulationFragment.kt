package com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sciencequest.R
import com.example.sciencequest.User.LessonLibrary.LessonDetailsFragmentDirections
import com.example.sciencequest.User.Quiz.ResultFragmentDirections
import com.example.sciencequest.databinding.FragmentSimulationBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SimulationFragment : Fragment() {

    private var _binding: FragmentSimulationBinding? = null
    private val binding get() = _binding!!
    private var activeLessonType = LessonType.QUEST // Tracks the currently active lesson
    private var simulationUrl: String? = null
    private var lessonId: String? = null
    private var topic: String? = null
    private var category: String? = null
    private var simulationTitle: String? = null
    private var userId: String? = null

    private val lessons = mutableListOf<Lesson>() // Store lessons dynamically

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimulationBinding.inflate(inflater, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)

        // Retrieve arguments
        arguments?.let {
            simulationUrl = it.getString("simulationUrl")
            lessonId = it.getString("lessonId")
            topic = it.getString("topic")
            category = it.getString("category")
        }

        // Check if the simulationUrl is valid
        if (simulationUrl.isNullOrEmpty()) {
            Log.e("SimulationFragment", "No simulation URL passed!")
            // Optionally, show a placeholder or error message in the UI
            // You could also navigate back or display a default simulation
            simulationUrl = "https://phet.colorado.edu/sims/html/states-of-matter/latest/states-of-matter_all.html" // Default URL
        }

        setupWebView()

        loadLessonDetails(lessonId!!)
        //setupNavigationButtons()

        return binding.root
    }

    private fun setupWebView() {
        val webView: WebView = binding.webViewSimulation
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        simulationUrl?.let { webView.loadUrl(it) }

    }

    private fun loadLessonDetails(lessonId: String) {
        val database = FirebaseDatabase.getInstance()
        val lessonRef = database.getReference("Lessons")
            .child(category!!)
            .child(topic!!)
            .child(lessonId)

        val userLessonRef = database.getReference("Users")
            .child(userId!!)
            .child("lessonProgress")
            .child(category!!)
            .child(topic!!)
            .child(lessonId!!)

        lessonRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val title = snapshot.child("title").getValue(String::class.java) ?: "Untitled"
                val content = snapshot.child("description").getValue(String::class.java) ?: "No content available"
                val videoId = snapshot.child("videoUrl").getValue(String::class.java)

                // Load the PhET simulation and Quiz links (if any)
                val phetSimulationSnapshot = snapshot.child("phetSimulation") // Fetching PhET simulation data
                val quizSnapshot = snapshot.child("quiz") // Fetching quiz data
                binding.titleTv.text = phetSimulationSnapshot.child("title").getValue(String::class.java) ?: ""


                // Fetch the lesson progress statuses from the "Users" node
                userLessonRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        val videoStatus = LessonStatus.valueOf(userSnapshot.child("videoStatus").getValue(String::class.java) ?: LessonStatus.UNTOUCHED.name)
                        val simulationStatus = LessonStatus.valueOf(userSnapshot.child("simulationStatus").getValue(String::class.java) ?: LessonStatus.UNTOUCHED.name)
                        val quizStatus = LessonStatus.valueOf(userSnapshot.child("quizStatus").getValue(String::class.java) ?: LessonStatus.UNTOUCHED.name)

                        // Clear the existing lessons list
                        lessons.clear()

                        // Video Lesson
                        lessons.add(
                            Lesson(
                                id = "1",
                                description = content,
                                title = title,
                                videoUrl = videoId ?: "",
                                //lessonStatus = LessonStatus.valueOf(snapshot.child("lessonStatus").getValue(String::class.java) ?: LessonStatus.UNTOUCHED.name),
                                lessonType = LessonType.VIDEO,
                                isEnabled = snapshot.child("isEnabled").getValue(Boolean::class.java) ?: true,
//                                phetSimulation = Simulation(title = "", url = "", lessonStatus = LessonStatus.UNTOUCHED, lessonType = LessonType.SIMULATION, isEnabled = false), // Empty simulation
//                                quiz = Quiz(title = "", quizId = "", lessonStatus = LessonStatus.UNTOUCHED, lessonType = LessonType.QUEST, isEnabled = false) // Empty quiz
                                phetSimulation = Simulation(title = "", url = "", lessonType = LessonType.SIMULATION, isEnabled = false), // Empty simulation
                                quiz = Quiz(title = "", quizId = "", lessonType = LessonType.QUEST, isEnabled = false),
                                videoStatus = videoStatus,
                                simulationStatus = simulationStatus,
                                quizStatus = quizStatus

                            )
                        )
                        // For simulation lesson
                        lessons.add(
                            Lesson(
                                id = "2",
                                description = content,
                                title = title,
                                videoUrl = "", // No video for simulation
                                //lessonStatus = LessonStatus.valueOf(snapshot.child("lessonStatus").getValue(String::class.java) ?: LessonStatus.UNTOUCHED.name),
                                lessonType = LessonType.SIMULATION,
                                isEnabled = snapshot.child("isEnabled").getValue(Boolean::class.java) ?: true,
                                phetSimulation = Simulation(
                                    title = phetSimulationSnapshot.child("title").getValue(String::class.java) ?: "",
                                    url = phetSimulationSnapshot.child("url").getValue(String::class.java) ?: "",
//                                    lessonStatus = LessonStatus.valueOf(phetSimulationSnapshot.child("lessonStatus").getValue(String::class.java) ?: LessonStatus.UNTOUCHED.name),
                                    lessonType = LessonType.SIMULATION,
                                    isEnabled = phetSimulationSnapshot.child("isEnabled").getValue(Boolean::class.java) ?: true
                                ),
//                                quiz = Quiz(title = "", quizId = "", lessonStatus = LessonStatus.UNTOUCHED, lessonType = LessonType.QUEST, isEnabled = false),
                                quiz = Quiz(title = "", quizId = "", lessonType = LessonType.QUEST, isEnabled = false),
                                videoStatus = videoStatus,
                                simulationStatus = simulationStatus,
                                quizStatus = quizStatus
                            )
                        )

                        // For quiz lesson
                        lessons.add(
                            Lesson(
                                id = "3",
                                description = content,
                                title = title,
                                videoUrl = "", // No video for quiz
                                //lessonStatus = LessonStatus.valueOf(snapshot.child("lessonStatus").getValue(String::class.java) ?: LessonStatus.UNTOUCHED.name),
                                lessonType = LessonType.QUEST,
                                isEnabled = snapshot.child("isEnabled").getValue(Boolean::class.java) ?: true,
                                //phetSimulation = Simulation(title = "", url = "", lessonStatus = LessonStatus.UNTOUCHED, lessonType = LessonType.SIMULATION, isEnabled = false), // Empty simulation
                                phetSimulation = Simulation(title = "", url = "", lessonType = LessonType.SIMULATION, isEnabled = false), // Empty simulation
                                quiz = Quiz(
                                    title = quizSnapshot.child("title").getValue(String::class.java) ?: "",
                                    quizId = quizSnapshot.child("quizId").getValue(String::class.java) ?: "",
//                                    lessonStatus = LessonStatus.valueOf(quizSnapshot.child("lessonStatus").getValue(String::class.java) ?: LessonStatus.UNTOUCHED.name),
                                    lessonType = LessonType.QUEST,
                                    isEnabled = quizSnapshot.child("isEnabled").getValue(Boolean::class.java) ?: true
                                ),
                                videoStatus = videoStatus,
                                simulationStatus = simulationStatus,
                                quizStatus = quizStatus
                            )
                        )


                        // Update the lesson icons list
                        setupLessonIcons()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error loading user lesson status", error.toException())
                    }
                })
            }


            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading lesson details", error.toException())
            }
        })
    }

    private fun setupLessonIcons() {
        // Change the layout manager to LinearLayoutManager for vertical orientation
        binding.lessonRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.lessonRecyclerView.adapter = LessonIconAdapter(
            lessons,
            "2"
        ) { lesson ->
            when (lesson.lessonType) {
                LessonType.QUEST -> {
                    checkAndUpdateSimulationStatus()

                    // Pass quizId to the QuizFragment for Quiz type lessons
                    val action = SimulationFragmentDirections
                        .actionSimulationFragmentToQuizDetailFragment(lesson.quiz.quizId, lessonId!!, topic!!, category!!, "lessonLibrary")
                    findNavController().navigate(action)
                }
                LessonType.VIDEO -> {
                    checkAndUpdateSimulationStatus()

                    val action = SimulationFragmentDirections
                        .actionSimulationFragmentToLessonDetailsFragment(lessonId!!, topic!!, category!!)
                    findNavController().navigate(action)
                    Log.d("phetUrl",lesson.phetSimulation.url)
                }
                LessonType.SIMULATION -> {

                }
            }
        }
    }

    private fun checkAndUpdateSimulationStatus() {
        val database = FirebaseDatabase.getInstance().getReference("Users")
        val userLessonRef = database.child(userId!!).child("lessonProgress").child(category!!).child(topic!!).child(lessonId!!)

        userLessonRef.child("simulationStatus")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentStatus = snapshot.getValue(String::class.java) ?: "UNTOUCHED"

                    if (currentStatus != "COMPLETED") {
                        val updates = hashMapOf<String, Any>(
                            "simulationStatus" to "COMPLETED"
                        )

                        userLessonRef.updateChildren(updates)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(), "Simulation completed!", Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to complete simulation.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        //Toast.makeText(requireContext(), "No score update needed.", Toast.LENGTH_SHORT).show()
                    }

//                    findNavController().navigate(
//                        ResultFragmentDirections.actionResultFragmentToLessonListFragment(category!!, topic!!)
//                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Database error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val webView: WebView = binding.webViewSimulation
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    requireActivity().onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
