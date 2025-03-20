package com.example.sciencequest.User.Home

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R
import com.example.sciencequest.User.LessonLibrary.Lesson
import com.example.sciencequest.User.LessonLibrary.LessonAdapter
import com.example.sciencequest.databinding.FragmentHomeBinding
import com.example.sciencequest.databinding.ItemCourseCardBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldRecyclerView: RecyclerView
    private lateinit var fieldAdapter: FieldAdapter
    private val fields = mutableListOf<Field>()
    private lateinit var database: FirebaseDatabase
    private lateinit var fieldsRef: DatabaseReference

    private lateinit var lessonRecyclerView: RecyclerView
    private lateinit var lessonAdapter: LessonAdapter
    private val lessons = mutableListOf<Lesson>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)
        val userEmail = sharedPreferences.getString("userEmail", null)
        val userDisplayName = sharedPreferences.getString("userDisplayName", null)
        val userRole = sharedPreferences.getString("userRole", null)
        val view = binding.root
        setupCourseCard(
            binding.courseBiology.root,
            binding.courseBiology.courseTitle,
            binding.courseBiology.courseDescription,
            binding.courseBiology.courseProgress,
            binding.courseBiology.courseImage,
            "Biology","Exploring Life and Living Systems", "87%", R.drawable.big_microscope
        )

        setupCourseCard(
            binding.courseChemistry.root,
            binding.courseChemistry.courseTitle,
            binding.courseChemistry.courseDescription,
            binding.courseChemistry.courseProgress,
            binding.courseChemistry.courseImage,
            "Chemistry","Understanding Chemical Reactions", "76%", R.drawable.big_flask
        )

        setupCourseCard(
            binding.coursePhysics.root,
            binding.coursePhysics.courseTitle,
            binding.coursePhysics.courseDescription,
            binding.coursePhysics.courseProgress,
            binding.coursePhysics.courseImage,
            "Physics","The Laws of Motion and Energy", "65%", R.drawable.big_atom
        )


        binding.userName.text = "Hello, $userDisplayName!"

        Log.d("UserDetails", "$userId \n $userEmail \n $userDisplayName \n $userRole" )

        database = FirebaseDatabase.getInstance()
        fieldsRef = database.getReference("Lessons")

        fieldRecyclerView = binding.fieldRv
        lessonRecyclerView = binding.lessonRv


        // Create a LinearLayoutManager with horizontal orientation
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        fieldRecyclerView.layoutManager = layoutManager
        lessonRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fieldAdapter = FieldAdapter(fields) { fieldName ->
            // Navigate to TopicFragment when a category is clicked
            val action = HomeFragmentDirections.actionHomeFragmentToTopicFragment(fieldName)
            findNavController().navigate(action)
        }
        lessonAdapter = LessonAdapter(lessons, R.color.cyan600) { lesson ->
            // Handle lesson item click, navigate to LessonDetailsFragment or another screen
            val action = HomeFragmentDirections.actionHomeFragmentToTopicFragment(lesson.title)
            findNavController().navigate(action)
        }
        // Set up RecyclerView
        fieldRecyclerView.adapter = fieldAdapter

        // Fetch fields from Firebase
        fetchFieldsFromDatabase()

        return view
        return binding.root
    }

    private fun setupCourseCard(
        root: View,
        titleView: TextView,
        descriptionView: TextView,
        progressView: TextView,
        imageView: ImageView,
        title: String,
        description: String,
        progress: String,
        imageRes: Int
    ) {
        titleView.text = title
        descriptionView.text = description
        progressView.text = "Progress: $progress"
        imageView.setImageResource(imageRes)

        root.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToTopicFragment(title)
            findNavController().navigate(action)
        }

    }

    private fun fetchFieldsFromDatabase() {
        fieldsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the list before adding new data
                fields.clear()

                for (fieldSnapshot in snapshot.children) {
                    // Get field data
                    val fieldName = fieldSnapshot.child("fieldName").getValue(String::class.java) ?: ""
                    val fieldImageUrl = fieldSnapshot.child("fieldImageUrl").getValue(String::class.java) ?: ""


                    // Add to the fields list
                    val field = Field(fieldName, fieldImageUrl)
                    fields.add(field)
                }

                // Notify the adapter that data has been updated
                fieldAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data", error.toException())
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Custom ItemDecoration to add spacing between RecyclerView items
    class ItemSpacingDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            // Add spacing to the left, top, right, and bottom of each item
            outRect.left = spacing
            outRect.right = spacing
            outRect.top = spacing
            outRect.bottom = spacing
        }
    }
}
