package com.example.sciencequest.User.LessonLibrary

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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R
import com.example.sciencequest.User.Home.HomeFragment.ItemSpacingDecoration
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TopicFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var topicAdapter: TopicAdapter
    private lateinit var progressBar: ProgressBar
    private val topics = mutableListOf<String>()
    private lateinit var category: String
    private lateinit var database: FirebaseDatabase
    private lateinit var fieldDescriptionTextView: TextView
    private lateinit var categoryTv: TextView
    private lateinit var fieldDescTv: TextView
    private lateinit var backButton: ImageButton

    private val categoryConfig = mapOf(
        "Physics" to CategoryConfig(
            fragmentBackgroundColor = R.color.darkBlueButton,
            itemBackgroundColor = R.color.darkBlueButton,
            fragmentImage = R.drawable.big_atom
        ),
        "Chemistry" to CategoryConfig(
            fragmentBackgroundColor = R.color.greenButton,
            itemBackgroundColor = R.color.greenButton,
            fragmentImage = R.drawable.big_flask
        ),
        "Biology" to CategoryConfig(
            fragmentBackgroundColor = R.color.blueButton,
            itemBackgroundColor = R.color.blueButton,
            fragmentImage = R.drawable.big_microscope
        ),
        "Math" to CategoryConfig(
            fragmentBackgroundColor = R.color.reddishPinkButton,
            itemBackgroundColor = R.color.reddishPinkButton,
            fragmentImage = R.drawable.big_growth
        ),
        "Earth" to CategoryConfig(
            fragmentBackgroundColor = R.color.pinkButton,
            itemBackgroundColor = R.color.pinkButton,
            fragmentImage = R.drawable.big_planet
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_topic, container, false)

        // Initialize RecyclerView and ProgressBar
        recyclerView = view.findViewById(R.id.topicRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        // Get the category passed as an argument
        category = arguments?.getString("category") ?: ""

        // Normalize category values
        category = when (category) {
            "Earth & Space" -> "Earth"
            "Math & Statistics" -> "Math"
            else -> category
        }

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
        val fieldImage = view.findViewById<ImageView>(R.id.fieldImg)
        fieldImage.setImageResource(config.fragmentImage)

        // Validate the category
        if (category.isEmpty()) {
            Log.e("TopicFragment", "Category argument is missing or empty")
            progressBar.visibility = View.GONE
            return view // Return early to avoid further processing
        }

        // Log the category for debugging
        Log.d("TopicFragment", "Selected category: $category")

        // Set up the adapter and layout manager
        topicAdapter = TopicAdapter(topics, config.itemBackgroundColor) { topic ->
            // Navigate to LessonListFragment when a topic is clicked
            val action = TopicFragmentDirections.actionTopicFragmentToLessonListFragment(category, topic)
            findNavController().navigate(action)
        }

        categoryTv = view.findViewById(R.id.categoryTv)

        if(category.equals("Math")){
            categoryTv.text = "Math & Statistics:"
        }else if(category.equals("Earth")){
            categoryTv.text = "Earth & Space:"
        }



        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = topicAdapter

        // Add spacing between items
        //recyclerView.addItemDecoration(ItemSpacingDecoration(16))  // 16px gap between items

        // Show the ProgressBar while loading
        progressBar.visibility = View.VISIBLE

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        fieldDescTv = view.findViewById(R.id.fieldDescTv)


        // Load topics for the selected category
        loadTopicsFromFirebase(category) { loadedTopics, desc ->
            topics.clear()
            topics.addAll(loadedTopics)
            topicAdapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
            fieldDescTv.text = desc
        }



        backButton = view.findViewById(R.id.backBtn)
        backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        return view
    }

    private fun loadTopicsFromFirebase(category: String, callback: (List<String>, String) -> Unit) {
        val topicsRef = database.getReference("Lessons").child(category)

        topicsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val topics = mutableListOf<String>()
                var fieldDescription = ""  // Variable to store fieldDescription

                for (topicSnapshot in snapshot.children) {
                    val topicKey = topicSnapshot.key ?: ""

                    if (topicKey == "fieldDescription") {
                        fieldDescription = topicSnapshot.value.toString() // Get description
                    } else if (topicKey != "fieldName" && topicKey != "fieldImageUrl") {
                        topics.add(topicKey) // Add valid topics
                    }
                }
                callback(topics, fieldDescription) // Pass both topics and description
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading topics", error.toException())
                progressBar.visibility = View.GONE
            }
        })
    }
}
