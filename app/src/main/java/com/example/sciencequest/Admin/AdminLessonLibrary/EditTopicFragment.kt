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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditTopicFragment : Fragment() {

    private lateinit var spinnerBranch: Spinner
    private lateinit var spinnerTopic: Spinner
    private lateinit var editTextLessonTitle: TextInputEditText
    private lateinit var editTextLessonDesc: TextInputEditText
    private lateinit var editTextVideoUrl: TextInputEditText
    private lateinit var checkBoxLessonEnabled: CheckBox
    private lateinit var buttonSubmit: Button
    private lateinit var buttonDelete: Button
    private lateinit var checkBoxQuizEnabled: CheckBox
    private lateinit var editTextQuizId: TextInputEditText
    private lateinit var editTextQuizTitle: TextInputEditText
    private lateinit var checkBoxPhetEnabled: CheckBox
    private lateinit var editTextPhetTitle: TextInputEditText
    private lateinit var editTextPhetUrl: TextInputEditText
    private lateinit var editTextTopicDesc: TextInputEditText
    private var lessonId: String? = null
    private var topic: String? = null
    private var branch: String? = null

    private lateinit var spinnerQuiz: Spinner
    private var selectedQuizId: String? = null


    private lateinit var database: DatabaseReference
    private val args: EditTopicFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_topic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference.child("Lessons")

        spinnerBranch = view.findViewById(R.id.spinner_branch)
        spinnerTopic = view.findViewById(R.id.spinner_existing_topics)
        editTextLessonTitle = view.findViewById(R.id.input_lesson_title)
        editTextLessonDesc = view.findViewById(R.id.input_lesson_desc)
        editTextVideoUrl = view.findViewById(R.id.input_video_url)
        checkBoxLessonEnabled = view.findViewById(R.id.input_lesson_enabled)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        buttonDelete = view.findViewById(R.id.buttonDelete)
        checkBoxQuizEnabled = view.findViewById(R.id.input_quiz_enabled)
//        editTextQuizId = view.findViewById(R.id.editTextQuizId)
//        editTextQuizTitle = view.findViewById(R.id.editTextQuizTitle)
        checkBoxPhetEnabled = view.findViewById(R.id.input_phet_enabled)
        editTextPhetTitle = view.findViewById(R.id.editTextPhetTitle)
        editTextPhetUrl = view.findViewById(R.id.input_phet_url)
        editTextTopicDesc = view.findViewById(R.id.input_topic_desc)
        spinnerQuiz = view.findViewById(R.id.spinner_quiz)

        val backBtn = view.findViewById<ImageButton>(R.id.backBtn)
        backBtn.setOnClickListener { findNavController().popBackStack() }

        // Retrieve arguments
        arguments?.let {
            lessonId = it.getString("lessonId")
            topic = it.getString("topic")
            branch = it.getString("branch")
        }

        loadLessonData(args.branch, args.topic, args.lessonId)

        buttonSubmit.setOnClickListener {
            updateLessonData()
        }

        buttonDelete.setOnClickListener {
            deleteLesson()
        }
    }

    private fun loadQuizzes(previouslySelectedQuizId: String?) {
        val quizRef = FirebaseDatabase.getInstance().reference.child("Quizzes")

        quizRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val quizList = mutableListOf<Pair<String, String>>() // (quizId, quizTitle)
                snapshot.children.forEach {
                    val quizId = it.key.toString()
                    val quizTitle = it.child("title").value?.toString() ?: "Untitled Quiz"
                    quizList.add(quizId to quizTitle)
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,
                    quizList.map { it.second } // Only display quiz titles
                )
                spinnerQuiz.adapter = adapter

                // Select the quiz that was previously assigned to the lesson
                val index = quizList.indexOfFirst { it.first == previouslySelectedQuizId }
                if (index != -1) spinnerQuiz.setSelection(index)

                // Store selected quizId when changed
                spinnerQuiz.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedQuizId = quizList[position].first // Now updates the class variable
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }.addOnFailureListener {
            Log.e("Firebase", "Failed to load quizzes: ${it.message}")
        }
    }



    private fun loadLessonData(branch: String, topic: String, lessonId: String) {
        val lessonRef = database.child(branch).child(topic).child(lessonId)
        val topicDescRef = database.child(branch).child(topic).child("topicDesc")  // Fetch topicDesc at the topic level

        //Log.d("Firebase", "Fetching data from: ${lessonRef.path}")

        lessonRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {

                // Main lesson data
                val title = snapshot.child("title").value?.toString() ?: ""
                val description = snapshot.child("description").value?.toString() ?: ""
                val videoUrl = snapshot.child("videoUrl").value?.toString() ?: ""
                val isEnabled = snapshot.child("isEnabled").value as? Boolean ?: false

                // Phet data (nested under phetSimulation)
                val phetEnabled = snapshot.child("phetSimulation").child("isEnabled").value as? Boolean ?: false
                val phetTitle = snapshot.child("phetSimulation").child("title").value?.toString() ?: ""
                val phetUrl = snapshot.child("phetSimulation").child("url").value?.toString() ?: ""

                // Quiz data (nested under quiz)
                val quizEnabled = snapshot.child("quiz").child("isEnabled").value as? Boolean ?: false
//                val quizId = snapshot.child("quiz").child("quizId").value?.toString() ?: ""
//                val quizTitle = snapshot.child("quiz").child("title").value?.toString() ?: ""

                val quizId = snapshot.child("quiz").child("quizId").value?.toString() ?: ""
                // Load quizzes and set selected one
                loadQuizzes(quizId)


                // Fetch topicDesc from the topic level
                topicDescRef.get().addOnSuccessListener { topicSnapshot ->
                    val topicDesc = topicSnapshot.value?.toString() ?: ""
                    editTextTopicDesc.setText(topicDesc)

                    // Populate fields
                    editTextLessonTitle.setText(title)
                    editTextLessonDesc.setText(description)
                    editTextVideoUrl.setText(videoUrl)
                    checkBoxLessonEnabled.isChecked = isEnabled

                    // Populate Phet fields
                    checkBoxPhetEnabled.isChecked = phetEnabled
                    editTextPhetTitle.setText(phetTitle)
                    editTextPhetUrl.setText(phetUrl)

                    // Populate Quiz fields
                    checkBoxQuizEnabled.isChecked = quizEnabled
//                    editTextQuizId.setText(quizId)
//                    editTextQuizTitle.setText(quizTitle)

                    // Set initial branch and topic
                    val fetchedBranch = snapshot.child("branch").value?.toString() ?: branch
                    val fetchedTopic = snapshot.child("topic").value?.toString() ?: topic

                    loadBranches(fetchedBranch)
                    loadTopics(fetchedBranch, fetchedTopic)
                }
            } else {
                Log.e("Firebase", "Lesson data not found for $branch/$topic/$lessonId")
                Toast.makeText(requireContext(), "Lesson not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to load lesson data: ${e.message}")
            Toast.makeText(requireContext(), "Error loading lesson data", Toast.LENGTH_SHORT).show()
        }
    }



    private fun loadBranches(selectedBranch: String) {
        val branches = listOf("Biology", "Physics", "Chemistry", "Earth", "Math")
        val adapter = ArrayAdapter(requireContext(), R.layout.big_spinner_item, branches)
        spinnerBranch.adapter = adapter

        // Automatically select the fetched branch
        val index = branches.indexOf(selectedBranch)
        if (index != -1) spinnerBranch.setSelection(index)

        // Load topics for the selected branch
        loadTopics(selectedBranch, "")
    }

    private fun loadTopics(branch: String, selectedTopic: String) {
        val topicRef = database.child(branch)
        topicRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val topics = snapshot.children
                    .filter { it.key != "fieldDescription" && it.key != "fieldImageUrl" && it.key != "fieldName" }
                    .map { it.key.toString() }

                val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, topics)
                spinnerTopic.adapter = adapter

                // Automatically select the fetched topic
                val index = topics.indexOf(selectedTopic)
                if (index != -1) spinnerTopic.setSelection(index)
            }
        }
    }


    private fun updateLessonData() {
        val newBranch = spinnerBranch.selectedItem.toString()
        val newTopic = spinnerTopic.selectedItem.toString()
        val lessonId = args.lessonId

        val lessonTitle = editTextLessonTitle.text.toString().trim()
        val lessonDesc = editTextLessonDesc.text.toString().trim()
        val videoUrl = editTextVideoUrl.text.toString().trim()
        val isLessonEnabled = checkBoxLessonEnabled.isChecked

        val isPhetEnabled = checkBoxPhetEnabled.isChecked
        val phetTitle = editTextPhetTitle.text.toString().trim()
        val phetUrl = editTextPhetUrl.text.toString().trim()

        val isQuizEnabled = checkBoxQuizEnabled.isChecked
//        val quizId = editTextQuizId.text.toString().trim()
//        val quizTitle = editTextQuizTitle.text.toString().trim()

        val topicDesc = editTextTopicDesc.text.toString().trim()

        val selectedQuizId = this.selectedQuizId ?: ""
        val selectedQuizTitle = spinnerQuiz.selectedItem.toString()

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

        // Move lesson if branch/topic is changed
        if (newBranch != args.branch || newTopic != args.topic) {
            val oldLessonRef = database.child(args.branch).child(args.topic).child(lessonId)
            val newLessonRef = database.child(newBranch).child(newTopic).child(lessonId)

            val oldTopicDescRef = database.child(args.branch).child(args.topic).child("topicDesc")
            val newTopicDescRef = database.child(newBranch).child(newTopic).child("topicDesc")

            oldLessonRef.removeValue().addOnSuccessListener {
                // Update lesson data in the new location
                newLessonRef.setValue(lessonData).addOnSuccessListener {
                    // Move topicDesc separately
                    oldTopicDescRef.removeValue().addOnSuccessListener {
                        newTopicDescRef.setValue(topicDesc).addOnSuccessListener {
                            Toast.makeText(requireContext(), "Lesson moved and updated successfully!", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        } else {
            // Update lesson data in the same branch/topic
            database.child(newBranch).child(newTopic).child(lessonId).setValue(lessonData)
                .addOnSuccessListener {
                    // Update topicDesc in the new location
                    database.child(newBranch).child(newTopic).child("topicDesc").setValue(topicDesc)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Lesson updated successfully!", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                }
        }
    }



    private fun deleteLesson() {
        val branch = args.branch
        val topic = args.topic
        val lessonId = args.lessonId

        database.child(branch).child(topic).child(lessonId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Lesson deleted successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to delete lesson: ${e.message}")
            }
    }
}
