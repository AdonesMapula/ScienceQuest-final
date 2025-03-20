package com.example.sciencequest.User.Quiz

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.sciencequest.R
import com.example.sciencequest.databinding.FragmentResultBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class ResultFragment : Fragment(R.layout.fragment_result) {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private var quizId: String? = null
    private var lessonId: String? = null
    private var topic: String? = null
    private var category: String? = null
    private var userId: String? = null
    private var from: String? = null
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultBinding.inflate(inflater, container, false)

        val sharedPreferences =
            requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null)

        arguments?.let {
            quizId = it.getString("quizId")
            lessonId = it.getString("lessonId")
            topic = it.getString("topic")
            category = it.getString("category")
            from = it.getString("from")
        }

        Log.d("categoryResult", "$category")

        // SafeArgs will automatically pass the arguments to the fragment
        val score = arguments?.getInt("score") ?: 0
        val totalQuestions = arguments?.getInt("totalQuestions") ?: 0
//        quizId = arguments?.getString("quizId") ?: return null

        // Display the score
        binding.scoreText.text = "$score"
        binding.overText.text = "/$totalQuestions"
        binding.topicText.text = "$topic"

        // Display a message based on score
        val message = when {
            score == totalQuestions -> "Perfect! All correct!"
            score >= totalQuestions * 0.7 -> "Great job! You passed."
            score >= totalQuestions * 0.5 -> "Not bad, but there's room for improvement."
            else -> "Better luck next time!"
        }
        binding.messageText.text = message

        // Set the Restart Button click listener
        binding.restartButton.setOnClickListener {
            restartQuiz()
        }

        binding.doneButton.setOnClickListener {
            checkAndUpdateScore(score, totalQuestions)
        }



        return binding.root
    }

    // Function to navigate back to the QuizFragment and restart the quiz
    private fun restartQuiz() {
        // Pass necessary arguments to restart the quiz (you can pass the quizId or reset it if needed)
        val action = ResultFragmentDirections.actionResultFragmentToQuizFragment(quizId!!, lessonId!!, topic!!, category!!)
        findNavController().navigate(action)
    }


    private fun checkAndUpdateScore(score: Int, totalQuestions: Int) {
        val userLessonRef = database.child(userId!!).child("lessonProgress").child(category!!).child(topic!!).child(lessonId!!)
        val userRef = database.child(userId!!)
        val categoryRef = database.child(userId!!).child("categoryTotalScore").child(category!!)

        userLessonRef.child("quizScore")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val previousScore = snapshot.getValue(Int::class.java) ?: 0

                    if (score > previousScore) {
                        // Update quizScore and quizStatus if the new score is higher
                        val updates = hashMapOf<String, Any>(
                            "quizScore" to score,
                            "quizStatus" to "COMPLETED"
                        )

                        userLessonRef.updateChildren(updates)
                            .addOnSuccessListener {
                                // Update totalScore in the user's profile
                                userRef.runTransaction(object : Transaction.Handler {
                                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                        val totalCompletedQuizzes = mutableData.child("totalCompletedQuizzes").getValue(Int::class.java) ?: 0
                                        val totalScore = mutableData.child("totalScore").getValue(Int::class.java) ?: 0

                                        // Adjust totalScore: Remove previous score and add the new one
                                        val newTotalScore = totalScore - previousScore + score

                                        mutableData.child("totalScore").value = newTotalScore

                                        return Transaction.success(mutableData)
                                    }

                                    override fun onComplete(
                                        error: DatabaseError?,
                                        committed: Boolean,
                                        currentData: DataSnapshot?
                                    ) {
                                        if (committed) {
                                            Toast.makeText(
                                                requireContext(), "New high score! Score updated.", Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to update user stats.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                })

                                // Update category total score
                                categoryRef.runTransaction(object : Transaction.Handler {
                                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                        val currentCategoryTotalScore = mutableData.getValue(Int::class.java) ?: 0

                                        // Adjust category total score by adding the new score
                                        val newCategoryTotalScore = currentCategoryTotalScore - previousScore + score

                                        mutableData.value = newCategoryTotalScore

                                        return Transaction.success(mutableData)
                                    }

                                    override fun onComplete(
                                        error: DatabaseError?,
                                        committed: Boolean,
                                        currentData: DataSnapshot?
                                    ) {
                                        if (committed) {
                                            Toast.makeText(
                                                requireContext(), "Category total score updated.", Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to update category score.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                })

                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to update score.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Your score wasn't higher. No update needed.", Toast.LENGTH_SHORT).show()
                    }

                    Log.d("fromValue", "$from, $category")
                    if (from == "lessonLibrary"){
                        findNavController().navigate(ResultFragmentDirections.actionResultFragmentToLessonListFragment(category!!, topic!!))
                    } else {
                        findNavController().navigate(ResultFragmentDirections.actionResultFragmentToQuizzesFragment(category!!))
                    }

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




}