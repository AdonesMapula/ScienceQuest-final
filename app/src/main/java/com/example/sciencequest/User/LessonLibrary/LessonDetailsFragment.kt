package com.example.sciencequest.User.LessonLibrary

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sciencequest.MainActivity
import com.example.sciencequest.R
import com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations.Lesson
import com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations.LessonIconAdapter
import com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations.LessonStatus
import com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations.LessonType
import com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations.Quiz
import com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations.Simulation
import com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations.SimulationFragmentDirections
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.example.sciencequest.databinding.FragmentLessonDetailsBinding

class LessonDetailsFragment : Fragment(R.layout.fragment_lesson_details) {
    private lateinit var binding: FragmentLessonDetailsBinding
    private var lessonId: String? = null
    private var topic: String? = null
    private var category: String? = null
    private lateinit var youTubePlayerView: YouTubePlayerView
    private var isFullscreen = false
    private var videoId: String? = null // Store the video ID for retention across rotations
    private var userId: String? = null
    private var activeLessonIndex = 0 // Tracks the currently active lesson

    private val lessons = mutableListOf<Lesson>() // Store lessons dynamically

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLessonDetailsBinding.inflate(inflater, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)

        // Retrieve arguments
        arguments?.let {
            lessonId = it.getString("lessonId")
            topic = it.getString("topic")
            category = it.getString("category")
        }


        setupLessonIcons()

        Log.d("DetailsArguments", "$lessonId $topic")

        // Load lesson details from Firebase
        lessonId?.let { loadLessonDetails(it) }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the YouTubePlayerView
        youTubePlayerView = view.findViewById(R.id.youtube_player_view)
        val detailsLayout: LinearLayout = view.findViewById(R.id.detailsLayout)
        val mainLayout: FrameLayout = view.findViewById(R.id.mainLayout)

        // Set enableAutomaticInitialization to false for manual initialization
        youTubePlayerView.enableAutomaticInitialization = false

        // Initialize IFramePlayerOptions with controls and fullscreen enabled
        val iFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(1) // Enable controls
            .fullscreen(1) // Enable fullscreen button
            .build()

        // Initialize YouTubePlayer and lifecycle observer
        lifecycle.addObserver(youTubePlayerView)

        // Restore video ID if available
        videoId = savedInstanceState?.getString("videoId") ?: "sJ-2X3rHtXw" // Default video ID if null

        // Set up FullscreenListener to handle fullscreen events
        youTubePlayerView.addFullscreenListener(object : FullscreenListener {
            override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                isFullscreen = true
                // Hide the BottomNavigationView when entering fullscreen
                (activity as? MainActivity)?.hideBottomNavigation()

                // Hide the player in the original view and show fullscreen view
                view.findViewById<FrameLayout>(R.id.full_screen_view_container).apply {
                    visibility = View.VISIBLE
                    removeAllViews()
                    addView(fullscreenView)
                }
                youTubePlayerView.visibility = View.GONE
                detailsLayout.visibility = View.GONE
                // Set padding (in pixels)
                mainLayout.setPadding(0, 0, 0, 0)
            }

            override fun onExitFullscreen() {
                isFullscreen = false
                // Show the BottomNavigationView when exiting fullscreen
                (activity as? MainActivity)?.showBottomNavigation()

                // Remove fullscreen view and show the player again
                view.findViewById<FrameLayout>(R.id.full_screen_view_container).apply {
                    visibility = View.GONE
                    removeAllViews()
                }
                youTubePlayerView.visibility = View.VISIBLE
                detailsLayout.visibility = View.VISIBLE
                val paddingInPx = 37 * resources.displayMetrics.density.toInt()
                // Set padding (in pixels)
                mainLayout.setPadding(0, paddingInPx, 0, 0)
            }
        })

        // Initialize YouTubePlayer manually with options
        youTubePlayerView.initialize(object : YouTubePlayerListener {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                videoId?.let { youTubePlayer.loadVideo(it, 0f) }
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                // Handle state changes if needed
                if (state == PlayerConstants.PlayerState.ENDED) {
                    checkAndUpdateVideoStatus() // Trigger when video ends
                }
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                // Track video duration if needed
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                // Handle video ID changes if needed
            }

            override fun onApiChange(youTubePlayer: YouTubePlayer) {
                // No implementation required
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                // Optional: If not using, you can leave empty or remove
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                // Handle errors if needed
            }

            override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {}
            override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {}
            override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
        }, iFramePlayerOptions)
    }

    private fun loadLessonDetails(lessonId: String) {
        val database = FirebaseDatabase.getInstance()
        val lessonRef = database.getReference("Lessons")
            .child(category.toString())
            .child(topic ?: "")
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

                binding.lessonTitle.text = title
                binding.lessonContent.text = content
                this@LessonDetailsFragment.videoId = videoId

                val phetSimulationSnapshot = snapshot.child("phetSimulation")
                val quizSnapshot = snapshot.child("quiz")

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
                                //lessonStatus = videoStatus,
                                lessonType = LessonType.VIDEO,
                                isEnabled = snapshot.child("isEnabled").getValue(Boolean::class.java) ?: true,
//                                phetSimulation = Simulation("", "", LessonStatus.UNTOUCHED, LessonType.SIMULATION, false),
//                                quiz = Quiz("", "", LessonStatus.UNTOUCHED, LessonType.QUEST, false),
                                phetSimulation = Simulation("", "", LessonType.SIMULATION, false),
                                quiz = Quiz("", "", LessonType.QUEST, false),
                                videoStatus = videoStatus,
                                simulationStatus = simulationStatus,
                                quizStatus = quizStatus
                            )
                        )

                        // Simulation Lesson
                        lessons.add(
                            Lesson(
                                id = "2",
                                description = content,
                                title = title,
                                videoUrl = "",
                                //lessonStatus = simulationStatus,
                                lessonType = LessonType.SIMULATION,
                                isEnabled = phetSimulationSnapshot.child("isEnabled").getValue(Boolean::class.java) ?: true,
                                phetSimulation = Simulation(
                                    title = phetSimulationSnapshot.child("title").getValue(String::class.java) ?: "",
                                    url = phetSimulationSnapshot.child("url").getValue(String::class.java) ?: "",
                                    //lessonStatus = simulationStatus,
                                    lessonType = LessonType.SIMULATION,
                                    isEnabled = phetSimulationSnapshot.child("isEnabled").getValue(Boolean::class.java) ?: true
                                ),
                                //quiz = Quiz("", "", LessonStatus.UNTOUCHED, LessonType.QUEST, false),
                                quiz = Quiz("", "", LessonType.QUEST, false),
                                videoStatus = videoStatus,
                                simulationStatus = simulationStatus,
                                quizStatus = quizStatus
                            )
                        )

                        // Quiz Lesson
                        lessons.add(
                            Lesson(
                                id = "3",
                                description = content,
                                title = title,
                                videoUrl = "",
                                //lessonStatus = quizStatus,
                                lessonType = LessonType.QUEST,
                                isEnabled = quizSnapshot.child("isEnabled").getValue(Boolean::class.java) ?: true,
                                //phetSimulation = Simulation("", "", LessonStatus.UNTOUCHED, LessonType.SIMULATION, false),
                                phetSimulation = Simulation("", "", LessonType.SIMULATION, false),
                                quiz = Quiz(
                                    title = quizSnapshot.child("title").getValue(String::class.java) ?: "",
                                    quizId = quizSnapshot.child("quizId").getValue(String::class.java) ?: "",
                                    //lessonStatus = quizStatus,
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
            lessons, "1"
        ) { lesson ->
            when (lesson.lessonType) {
                LessonType.QUEST -> {
                    // Pass quizId to the QuizFragment for Quiz type lessons
                    val action = LessonDetailsFragmentDirections
                        .actionLessonDetailsFragmentToQuizDetailFragment(lesson.quiz.quizId, lessonId!!, topic!!, category!!, "lessonLibrary")
                    findNavController().navigate(action)
                }
                LessonType.VIDEO -> {
                    // Handle video lesson, you can add more logic for video lessons if needed
                    // For example, navigate to video detail fragment
                }
                LessonType.SIMULATION -> {
                    // Pass simulationUrl to the SimulationFragment for Simulation type lessons
                    val action = LessonDetailsFragmentDirections
                        .actionLessonDetailsFragmentToSimulationFragment(lesson.phetSimulation.url, lessonId!!, topic!!, category!!, lesson.quiz.quizId)
                    findNavController().navigate(action)
                    Log.d("phetUrl",lesson.phetSimulation.url)
                }
            }
        }
    }

    private fun checkAndUpdateVideoStatus() {
        val database = FirebaseDatabase.getInstance().getReference("Users")
        val userLessonRef = database.child(userId!!).child("lessonProgress").child(category!!).child(topic!!).child(lessonId!!)

        userLessonRef.child("videoStatus")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentStatus = snapshot.getValue(String::class.java) ?: "UNTOUCHED"

                    if (currentStatus != "COMPLETED") {
                        val updates = hashMapOf<String, Any>(
                            "videoStatus" to "COMPLETED"
                        )

                        userLessonRef.updateChildren(updates)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(), "Video completed!", Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to complete video.",
                                    Toast.LENGTH_SHORT
                                ).show()
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
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the video ID to restore on rotation
        outState.putString("videoId", videoId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the YouTubePlayer to avoid memory leaks
        youTubePlayerView.release()
    }
}
