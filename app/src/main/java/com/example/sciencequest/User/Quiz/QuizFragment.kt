package com.example.sciencequest.User.Quiz

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sciencequest.R
import com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations.SimulationFragmentDirections
import com.example.sciencequest.databinding.FragmentQuizBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private var questionList: List<Question> = listOf()
    private var currentQuestionIndex = 0
    private var score = 0
    private var quizId:String? = null
    private var lessonId: String? = null
    private var topic: String? = null
    private var category: String? = null


    // Firebase Realtime Database instance
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var quizReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)

        arguments?.let {
            quizId = it.getString("quizId")
            lessonId = it.getString("lessonId")
            topic = it.getString("topic")
            category = it.getString("category")
        }

        // Assuming quizId is passed in the arguments
        //quizId = arguments?.getString("quizId") ?: return null
        quizReference = database.getReference("Quizzes/$quizId")

        // Show loading message or indicator
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.questionLayout.visibility = View.GONE

        // Fetch quiz data from Firebase
        fetchQuizFromFirebase()

        // Set up the click listeners for the answer buttons (MCQ)
        binding.optionButton1.setOnClickListener { checkAnswer(binding.optionButton1.text.toString(), 1) }
        binding.optionButton2.setOnClickListener { checkAnswer(binding.optionButton2.text.toString(), 2) }
        binding.optionButton3.setOnClickListener { checkAnswer(binding.optionButton3.text.toString(), 3) }
        binding.optionButton4.setOnClickListener { checkAnswer(binding.optionButton4.text.toString(), 4) }

        // Set up the click listener for the next button (or complete button)
        binding.nextButton.setOnClickListener {
            if (currentQuestionIndex >= questionList.size) {
                showResult() // If it's the last question, show results
            } else {
                nextQuestion() // Otherwise, move to the next question
            }
        }

        // Handle settings button click
        binding.settingsButton.setOnClickListener {
            val action = QuizFragmentDirections
                .actionQuizFragmentToQuizSettingsFragment(
                    currentProgress = currentQuestionIndex,
                    totalQuestions = questionList.size
                )
            findNavController().navigate(action)
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }


        return binding.root
    }


    private fun fetchQuizFromFirebase() {
        val quizId = arguments?.getString("quizId") ?: return
        Log.d("QuizFragment", "Fetching quiz with ID: $quizId")

        quizReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val quiz = snapshot.getValue(Quiz::class.java)
                    quiz?.let {
                        questionList = it.questions

                        // Randomize the questions
                        questionList = questionList.shuffled()

                        Log.d("QuizFragment", "Quiz data fetched: ${questionList.size} questions")

                        if (questionList.isNotEmpty()) {
                            binding.loadingIndicator.visibility = View.GONE
                            binding.questionLayout.visibility = View.VISIBLE
                            loadQuestion() // Load the first question
                        } else {
                            Toast.makeText(context, "No questions available", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "No quiz data available", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error fetching quiz data", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loadQuestion() {
        if (questionList.isNotEmpty() && currentQuestionIndex < questionList.size) {
            val currentQuestion = questionList[currentQuestionIndex]
            binding.apply {
                questionText.text = currentQuestion.question
                optionButton1.text = currentQuestion.options.getOrElse(0) { "" }
                optionButton2.text = currentQuestion.options.getOrElse(1) { "" }
                optionButton3.text = currentQuestion.options.getOrElse(2) { "" }
                optionButton4.text = currentQuestion.options.getOrElse(3) { "" }

                // Reset button colors (or any feedback mechanism)
                resetButtonColors()

                // Hide next button initially
                nextButton.visibility = View.GONE

                // Hide unused buttons if it's a True/False question
                if (currentQuestion.options.size == 2) { // Assuming True/False questions have 2 options
                    optionButton3.visibility = View.GONE
                    optionButton4.visibility = View.GONE
                } else {
                    optionButton3.visibility = View.VISIBLE
                    optionButton4.visibility = View.VISIBLE
                }

                binding.quizProgressBar.max = questionList.size
                binding.quizProgressBar.progress = currentQuestionIndex

                // Update progress percentage
                val progressPercentage = ((currentQuestionIndex.toFloat() / questionList.size) * 100).toInt()
                binding.quizProgressPercentage.text = "$progressPercentage%"

                // If it's the last question, change "Next" button to "Complete"
                if (currentQuestionIndex == questionList.size - 1) {
                    nextButton.text = getString(R.string.complete_button_text) // "Complete"
                }
            }
        } else {
            // Handle case if no more questions
            Toast.makeText(context, "No more questions or something went wrong", Toast.LENGTH_SHORT).show()
        }
    }


    private fun resetButtonColors() {
        // Define the colors
        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.blueButton),
            ContextCompat.getColor(requireContext(), R.color.pinkButton),
            ContextCompat.getColor(requireContext(), R.color.greenButton),
            ContextCompat.getColor(requireContext(), R.color.lightBlueButton)
        )

        // Shuffle the colors to randomize the order
        val shuffledColors = colors.shuffled()

        // Apply the colors to the buttons' background tint
        binding.optionButton1.backgroundTintList = ColorStateList.valueOf(shuffledColors[0])
        binding.optionButton2.backgroundTintList = ColorStateList.valueOf(shuffledColors[1])
        binding.optionButton3.backgroundTintList = ColorStateList.valueOf(shuffledColors[2])
        binding.optionButton4.backgroundTintList = ColorStateList.valueOf(shuffledColors[3])

        // Randomize and apply one of the colors to the question container's background tint
        val randomColor = shuffledColors[Random.nextInt(shuffledColors.size)]
        binding.questionContainer.backgroundTintList = ColorStateList.valueOf(randomColor)
    }

    private fun checkAnswer(selectedAnswer: String, selectedOption: Int) {
        if (questionList.isEmpty()) {
            // Guard clause: if no questions, return early
            Toast.makeText(context, "No questions loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        val currentQuestion = questionList[currentQuestionIndex]
        val isCorrect = selectedAnswer == currentQuestion.correct

        // Update score if the answer is correct
        if (isCorrect) {
            score++
        }

        // Show feedback
        showAnswerFeedback(selectedOption, isCorrect)

        // Disable answer buttons to prevent changes
        disableAnswerButtons()

        // Show the "Next" button
        binding.nextButton.visibility = View.VISIBLE
    }

    private fun showAnswerFeedback(selectedOption: Int, isCorrect: Boolean) {
        val currentQuestion = questionList[currentQuestionIndex]

        // Highlight the selected answer button
        val selectedButton = when (selectedOption) {
            1 -> binding.optionButton1
            2 -> binding.optionButton2
            3 -> binding.optionButton3
            4 -> binding.optionButton4
            else -> null
        }

        selectedButton?.let {
            if (isCorrect) {
                it.setBackgroundColor(resources.getColor(android.R.color.holo_green_light)) // Green for correct
            } else {
                it.setBackgroundColor(resources.getColor(android.R.color.holo_red_light)) // Red for incorrect
            }
        }

        // Show the correct answer if user gets it wrong
        if (!isCorrect) {
            when (currentQuestion.correct) {
                currentQuestion.options[0] -> binding.optionButton1.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                currentQuestion.options[1] -> binding.optionButton2.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                currentQuestion.options[2] -> binding.optionButton3.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                currentQuestion.options[3] -> binding.optionButton4.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
            }
        }
    }

    private fun nextQuestion() {
        // Reset button colors and prepare for the next question
        resetButtonColors()

        // Hide "Next" button again
        binding.nextButton.visibility = View.GONE

        // Move to the next question only if it's within bounds
        currentQuestionIndex++

        binding.quizProgressBar.progress = currentQuestionIndex

        // Update progress percentage
        val progressPercentage = ((currentQuestionIndex.toFloat() / questionList.size) * 100).toInt()
        binding.quizProgressPercentage.text = "(${currentQuestionIndex} of ${questionList.size})"

        if (currentQuestionIndex < questionList.size) {
            loadQuestion()
            enableAnswerButtons()  // Enable the answer buttons again for the next question
        } else {
            showResult() // Finish the quiz if all questions are answered
        }
    }

    private fun enableAnswerButtons() {
        binding.optionButton1.isEnabled = true
        binding.optionButton2.isEnabled = true
        binding.optionButton3.isEnabled = true
        binding.optionButton4.isEnabled = true
    }

    private fun disableAnswerButtons() {
        binding.optionButton1.isEnabled = false
        binding.optionButton2.isEnabled = false
        binding.optionButton3.isEnabled = false
        binding.optionButton4.isEnabled = false
    }

//    private fun showResult() {
//        // Show results page (you can either use a new fragment or a dialog)
//        val action = QuizFragmentDirections
//            .actionQuizFragmentToResultFragment(score, questionList.size, quizId!!, lessonId!!, topic!!, category!!)
//        findNavController().navigate(action)
//
//    }
    private fun showResult() {
        // Ensure required arguments are not null
        Log.d("QuizData", "$quizId, $lessonId, $topic, $category")
        if (quizId != null && lessonId != null && topic != null && category != null) {
            // Show results page (you can either use a new fragment or a dialog)
            val action = QuizFragmentDirections
                .actionQuizFragmentToResultFragment(
                    score, questionList.size, quizId!!, lessonId!!, topic!!, category!!, "quizzes"
                )
            findNavController().navigate(action)
        } else {
            // Handle the case where arguments are missing
            Toast.makeText(context, "Unable to show results: Missing quiz data", Toast.LENGTH_SHORT).show()
        }
    }


}

