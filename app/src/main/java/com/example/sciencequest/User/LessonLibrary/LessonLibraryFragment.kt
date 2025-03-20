package com.example.sciencequest.User.LessonLibrary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sciencequest.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LessonLibraryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<String>()
    private lateinit var progressBar: ProgressBar
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lesson_library, container, false)

        // Initialize RecyclerView and ProgressBar
        recyclerView = view.findViewById(R.id.lessonRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        // Set up the adapter and layout manager
        categoryAdapter = CategoryAdapter(categories) { category ->
            // Navigate to TopicFragment when a category is clicked
            val action = LessonLibraryFragmentDirections.actionLessonLibraryFragmentToTopicFragment(category)
            findNavController().navigate(action)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = categoryAdapter

        // Show the ProgressBar while loading
        progressBar.visibility = View.VISIBLE

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()

        // Load lesson categories from Firebase
        loadCategoriesFromFirebase { loadedCategories ->
            categories.clear()
            categories.addAll(loadedCategories)
            categoryAdapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
        }

        return view
    }

    private fun loadCategoriesFromFirebase(callback: (List<String>) -> Unit) {
        val categoriesRef = database.getReference("Lessons")

        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<String>()
                for (categorySnapshot in snapshot.children) {
                    categories.add(categorySnapshot.key ?: "")
                }
                callback(categories)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error loading categories", error.toException())
                progressBar.visibility = View.GONE
            }
        })
    }
}
