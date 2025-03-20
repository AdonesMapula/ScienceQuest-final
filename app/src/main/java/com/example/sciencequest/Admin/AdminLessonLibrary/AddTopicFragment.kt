package com.example.sciencequest.Admin.AdminLessonLibrary

import com.example.sciencequest.R

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class AddTopicFragment : Fragment() {

    private lateinit var spinnerBranch: Spinner
    private lateinit var spinnerTopics: Spinner
    private lateinit var quizSpinner: Spinner
    private lateinit var checkBoxNewTopic: CheckBox
    private lateinit var editTextNewTopicTitle: TextInputEditText
    private lateinit var editTextNewTopicDesc: TextInputEditText
    private lateinit var editTextLessonTitle: TextInputEditText
    private lateinit var editTextLessonDesc: TextInputEditText
    private lateinit var editTextVideoUrl: TextInputEditText
    private lateinit var checkBoxLessonEnabled: CheckBox
    private lateinit var checkBoxQuizEnabled: CheckBox
    private lateinit var editTextQuizId: TextInputEditText
    private lateinit var editTextQuizTitle: TextInputEditText
    private lateinit var checkBoxPhetEnabled: CheckBox
    private lateinit var editTextPhetTitle: TextInputEditText
    private lateinit var editTextPhetUrl: TextInputEditText
    private lateinit var buttonSubmit: Button
    private var selectedQuizId: String? = null
    private var selectedQuizTitle: String? = null



    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_topic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference.child("Lessons")


        spinnerBranch = view.findViewById(R.id.spinner_branch)
        spinnerTopics = view.findViewById(R.id.spinner_existing_topics)
        checkBoxNewTopic = view.findViewById(R.id.checkbox_new_topic)
        editTextNewTopicTitle = view.findViewById(R.id.editText_new_topic_title)
        editTextNewTopicDesc = view.findViewById(R.id.editText_new_topic_desc)
        editTextLessonTitle = view.findViewById(R.id.input_lesson_title)
        editTextLessonDesc = view.findViewById(R.id.input_lesson_desc)
        editTextVideoUrl = view.findViewById(R.id.input_video_url)
        checkBoxLessonEnabled = view.findViewById(R.id.input_lesson_enabled)
        checkBoxQuizEnabled = view.findViewById(R.id.input_quiz_enabled)
//        editTextQuizId = view.findViewById(R.id.editTextQuizId)
//        editTextQuizTitle = view.findViewById(R.id.editTextQuizTitle)
        checkBoxPhetEnabled = view.findViewById(R.id.input_phet_enabled)
        editTextPhetTitle = view.findViewById(R.id.editTextPhetTitle)
        editTextPhetUrl = view.findViewById(R.id.input_phet_url)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        quizSpinner = view.findViewById(R.id.quizSpinner)

        fetchQuizzes()

        checkBoxPhetEnabled.setOnCheckedChangeListener { _, isChecked ->
            editTextPhetUrl.visibility = if (isChecked) View.VISIBLE else View.GONE
            editTextPhetTitle.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        checkBoxLessonEnabled.setOnCheckedChangeListener { _, isChecked ->
            editTextVideoUrl.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        checkBoxQuizEnabled.setOnCheckedChangeListener { _, isChecked ->
            quizSpinner.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        checkBoxNewTopic.setOnCheckedChangeListener { _, isChecked ->
            spinnerTopics.visibility = if (isChecked) View.GONE else View.VISIBLE
            editTextNewTopicTitle.visibility = if (isChecked) View.VISIBLE else View.GONE
            editTextNewTopicDesc.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Step 1: Populate Science Branch Spinner
        val branches = listOf("Biology", "Chemistry", "Physics", "Earth", "Math")
        val branchAdapter = ArrayAdapter(requireContext(), R.layout.big_spinner_item, branches)
        branchAdapter.setDropDownViewResource(R.layout.big_spinner_item)
        spinnerBranch.adapter = branchAdapter

        // Step 2: When a branch is selected, load topics from Firebase
        spinnerBranch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedBranch = branches[position]
                loadTopicsForBranch(selectedBranch)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val backBtn = view.findViewById<ImageButton>(R.id.backBtn)
        backBtn.setOnClickListener { findNavController().popBackStack() }

        buttonSubmit.setOnClickListener {
            saveTopicAndLesson()
        }
    }

    private fun fetchQuizzes() {
        val quizzesRef = FirebaseDatabase.getInstance().getReference("Quizzes")

        quizzesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val quizList = mutableListOf<Pair<String, String>>() // Store quizId and quizTitle

                for (quizSnapshot in snapshot.children) {
                    val quizId = quizSnapshot.child("id").value.toString()
                    val quizTitle = quizSnapshot.child("title").value.toString()
                    quizList.add(Pair(quizId, quizTitle))
                }

                // Update dropdown or list UI with quizList
                updateQuizDropdown(quizList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch quizzes: ${error.message}")
            }
        })
    }

    private fun updateQuizDropdown(quizList: List<Pair<String, String>>) {


        val quizTitles = quizList.map { it.second } // Extract titles for dropdown
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, quizTitles)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        quizSpinner.adapter = adapter

        quizSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedQuiz = quizList[position]
                selectedQuizId = selectedQuiz.first
                selectedQuizTitle = selectedQuiz.second
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }



    private fun loadTopicsForBranch(branch: String) {
        database.child(branch).get().addOnSuccessListener { snapshot ->
            val topicsList = mutableListOf<String>()

            if (snapshot.exists()) {
                for (topicSnapshot in snapshot.children) {
                    // Only add topic keys (excluding fieldDescription, fieldImageUrl, and fieldName)
                    val topicKey = topicSnapshot.key ?: ""
                    if (topicKey != "fieldDescription" && topicKey != "fieldImageUrl" && topicKey != "fieldName") {
                        topicsList.add(topicKey)
                    }
                }
            } else {
                topicsList.add("No topics available")
            }

            val topicAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, topicsList)
            topicAdapter.setDropDownViewResource(R.layout.spinner_item)
            spinnerTopics.adapter = topicAdapter
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to load topics: ${e.message}")
        }
    }



    fun saveTopicAndLesson() {
        val branchName = spinnerBranch.selectedItem.toString()
        val isNewTopic = checkBoxNewTopic.isChecked
        val topicName = if (isNewTopic) {
            editTextNewTopicTitle.text.toString().trim()
        } else {
            spinnerTopics.selectedItem.toString()
        }
        val topicDesc = editTextNewTopicDesc.text.toString().trim()
        val lessonId = database.child(branchName).child(topicName).push().key ?: return
        val lessonTitle = editTextLessonTitle.text.toString().trim()
        val lessonDesc = editTextLessonDesc.text.toString().trim()
        val videoUrl = editTextVideoUrl.text.toString().trim()
        val isLessonEnabled = checkBoxLessonEnabled.isChecked
        val isQuizEnabled = checkBoxQuizEnabled.isChecked
//        val quizId = editTextQuizId.text.toString().trim()
//        val quizTitle = editTextQuizTitle.text.toString().trim()
        val isPhetEnabled = checkBoxPhetEnabled.isChecked
        val phetTitle = editTextPhetTitle.text.toString().trim()
        val phetUrl = editTextPhetUrl.text.toString().trim()

        val lessonData = mapOf(
            "title" to lessonTitle,
            "description" to lessonDesc,
            "isEnabled" to isLessonEnabled,
            "lessonType" to "VIDEO",
            "videoUrl" to videoUrl,
            "phetSimulation" to mapOf(
                "isEnabled" to isPhetEnabled,
                "lessonType" to "SIMULATION",
                "title" to phetTitle,
                "url" to phetUrl
            ),
            "quiz" to mapOf(
                "isEnabled" to isQuizEnabled,
                "lessonType" to "QUEST",
                "quizId" to selectedQuizId,
                "title" to selectedQuizTitle
            )
        )

        val topicRef = database.child(branchName).child(topicName)

        topicRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists() || isNewTopic) {
                val topicInfo = mapOf("topicDesc" to topicDesc)
                topicRef.setValue(topicInfo)
            }

            topicRef.child(lessonId).setValue(lessonData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Lesson saved successfully!")
                    Toast.makeText(requireContext(), "Lesson saved successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to save lesson: ${e.message}")
                    Toast.makeText(requireContext(), "Failed to save lesson!", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to check topic existence: ${e.message}")
        }
    }


}
