package com.example.sciencequest.User.LessonLibrary

import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.sciencequest.R
import com.example.sciencequest.databinding.FragmentLessonDetailsBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
class SampleVideoPlayer : Fragment() {

    private lateinit var binding: FragmentLessonDetailsBinding
    private var lessonId: String? = null
    private var topic: String? = null
    private var category: String? = null
    private lateinit var playerView: PlayerView
    private lateinit var fullscreenButton: ImageButton
    private lateinit var exoPlayer: ExoPlayer
    private var isFullscreen = false
    private var videoInitialized = false
    private var videoUrl: String? = null // Store the video URL here

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLessonDetailsBinding.inflate(inflater, container, false)
        //lessonId = arguments?.getString("lessonId")
        // Retrieve arguments
        arguments?.let {
            lessonId = it.getString("lessonId")
            topic = it.getString("topic")
            category = it.getString("category")
        }

        Log.d("DetailsArguments", lessonId + " " + topic)

        // Initialize the ExoPlayer view
        playerView = binding.root.findViewById(R.id.exoPlayerView)
        fullscreenButton = binding.root.findViewById(R.id.fullscreenButton)

        // Restore fullscreen state if saved
        savedInstanceState?.let {
            isFullscreen = it.getBoolean("isFullscreen", false)
            videoUrl = it.getString("videoUrl") // Restore the video URL
            restoreFullscreenState(isFullscreen)
        }

        // Load lesson details from Firebase
        lessonId?.let { loadLessonDetails(it) }

        // Set fullscreen button click listener
        fullscreenButton.setOnClickListener {
            toggleFullscreen()
        }

        return binding.root
    }

    private fun loadLessonDetails(lessonId: String) {
        val database = FirebaseDatabase.getInstance()
        val lessonRef = database.getReference("Lessons")
            .child(category.toString())
            .child(topic ?: "")
            .child(lessonId)

        lessonRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val title = snapshot.child("title").getValue(String::class.java) ?: "Untitled"
                val content = snapshot.child("description").getValue(String::class.java) ?: "No content available"
                val videoId = snapshot.child("videoUrl").getValue(String::class.java)

                // Save the video URL
                val videoUrl = videoId?.let { "https://drive.google.com/uc?export=download&id=$it" }

                binding.lessonTitle.text = title
                binding.lessonContent.text = content

                // Initialize the player only if the video is not initialized yet
                if (!videoInitialized && videoUrl != null) {
                    initializeExoPlayer(videoUrl)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading lesson details", error.toException())
            }
        })
    }


    private fun initializeExoPlayer(videoUrl: String) {
        exoPlayer = ExoPlayer.Builder(requireContext()).build()

        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        playerView.player = exoPlayer
        videoInitialized = true
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        restoreFullscreenState(isFullscreen)
    }

    private fun restoreFullscreenState(isFullscreen: Boolean) {
        val margin = if (isFullscreen) 0 else context?.dpToPx(16f)?.toInt() ?: 16

        if (isFullscreen) {
            // Enter fullscreen
            playerView.updateLayoutParams<ViewGroup.LayoutParams> {
                height = ViewGroup.LayoutParams.MATCH_PARENT
                width = ViewGroup.LayoutParams.MATCH_PARENT
            }

            // Remove margins (set to 0)
            (playerView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(0, 0, 0, 0)
            }

            // Hide other views like content and title
            binding.lessonTitle.visibility = View.GONE
            binding.lessonContent.visibility = View.GONE

            fullscreenButton.setImageResource(R.drawable.baseline_fullscreen_exit_24)  // Set to exit fullscreen icon
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            // Exit fullscreen
            playerView.updateLayoutParams<ViewGroup.LayoutParams> {
                height = context?.dpToPx(200f)?.toInt() ?: 200  // Default height for portrait mode
                width = ViewGroup.LayoutParams.MATCH_PARENT
            }

            // Set margins to 16dp
            (playerView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(margin, margin, margin, margin)
            }

            // Show other views like content and title
            binding.lessonTitle.visibility = View.VISIBLE
            binding.lessonContent.visibility = View.VISIBLE

            fullscreenButton.setImageResource(R.drawable.baseline_fullscreen_24)  // Set to fullscreen icon
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // Request a layout update to ensure player view size changes
        playerView.requestLayout()
    }



    // dpToPx function for context
    private fun Context.dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }

    override fun onStop() {
        super.onStop()
        // Release the player when the fragment is stopped
        exoPlayer.release()
        // Ensure the orientation switches back to portrait when the fragment is stopped
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // Save the fullscreen state and video URL before the fragment is destroyed
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isFullscreen", isFullscreen)
        outState.putString("videoUrl", videoUrl)  // Save the video URL
    }
}
