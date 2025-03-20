package com.example.sciencequest.Admin.AdminLessonLibrary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sciencequest.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminLessonListFragment : Fragment() {
    private lateinit var spinnerBranch: Spinner
    private lateinit var expandableListView: ExpandableListView
    private lateinit var database: DatabaseReference
    private lateinit var topicAdapter: ExpandableListAdapter
    private var topicList = mutableListOf<String>()
    private var lessonListMap = mutableMapOf<String, MutableList<Map<String, String>>>()
    private var selectedBranch = "Biology"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_admin_lesson_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerBranch = view.findViewById(R.id.spinner_branch)
        expandableListView = view.findViewById(R.id.expandableListView)

        database = FirebaseDatabase.getInstance().getReference("Lessons")

        val branches = listOf("Biology", "Physics", "Chemistry", "Earth", "Math")
        val branchAdapter = ArrayAdapter(requireContext(), R.layout.big_spinner_item, branches)
        // Apply a dropdown style (optional)
        branchAdapter.setDropDownViewResource(R.layout.big_spinner_item)
        spinnerBranch.adapter = branchAdapter

        spinnerBranch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBranch = branches[position]
                loadLessons()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val topic = topicList[groupPosition]
            val lessonList = lessonListMap[topic] ?: return@setOnChildClickListener false

            // Get the lesson data (lessonId and lessonTitle)
            val lesson = lessonList[childPosition]
            val lessonId = lesson["lessonId"] ?: return@setOnChildClickListener false
            val lessonTitle = lesson["lessonTitle"] ?: return@setOnChildClickListener false

            Log.d("AddLesson", "$selectedBranch, $topic, $lessonTitle, $lessonId")
            val action = AdminLessonListFragmentDirections
                .actionAdminLessonListFragmentToEditTopicFragment(selectedBranch, topic, lessonId)

            findNavController().navigate(action)
            true
        }

        val addLessonBtn = view.findViewById<ImageButton>(R.id.addLessonBtn)
        addLessonBtn.setOnClickListener {
            findNavController().navigate(AdminLessonListFragmentDirections.actionAdminLessonListFragmentToFragmentAddTopic())
        }


    }

    private fun loadLessons() {
        database.child(selectedBranch).get().addOnSuccessListener { snapshot ->
            topicList.clear()
            lessonListMap.clear()

            snapshot.children.forEach { topicSnapshot ->
                if (topicSnapshot.key == "fieldDescription" ||
                    topicSnapshot.key == "fieldImageUrl" || topicSnapshot.key == "fieldName") {
                    return@forEach // Skip unwanted fields
                }

                val topicName = topicSnapshot.key.toString()
                val lessonList = mutableListOf<Map<String, String>>() // Store lesson data as a map

                topicSnapshot.children.forEach { lessonSnapshot ->
                    if (lessonSnapshot.key == "topicDesc") {
                        return@forEach // Skip unwanted fields
                    }

                    // Store both lessonId and lessonTitle
                    val lessonId = lessonSnapshot.key.toString()
                    val lessonTitle = lessonSnapshot.child("title").value.toString()

                    lessonList.add(mapOf("lessonId" to lessonId, "lessonTitle" to lessonTitle))
                }

                // Add the topic name and corresponding lessons to the map
                topicList.add(topicName)
                lessonListMap[topicName] = lessonList
            }

            // Update the expandable list view adapter
            topicAdapter = ExpandableListAdapter(requireContext(), topicList, lessonListMap)
            expandableListView.setAdapter(topicAdapter)
        }
    }




}

