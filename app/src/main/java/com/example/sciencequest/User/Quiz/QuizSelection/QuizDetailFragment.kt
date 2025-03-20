package com.example.sciencequest.User.Quiz.QuizSelection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sciencequest.R

class QuizDetailFragment : Fragment() {
    private var quizId: String? = null
    private var lessonId: String? = null
    private var topic: String? = null
    private var category: String? = null
    private var from: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val numQuestions: TextView = view.findViewById(R.id.numQuestions)
        val startQuizButton: Button = view.findViewById(R.id.startQuizButton)
        val backButton: ImageButton = view.findViewById(R.id.backBtn)

        arguments?.let {
            quizId = it.getString("quizId")
            lessonId = it.getString("lessonId")
            topic = it.getString("topic")
            category = it.getString("category")
            from = it.getString("from")
        }
        // Fetch and display quiz details based on quizId
        numQuestions.text = "10 Questions" // Replace with actual data
        Log.d("QuizData", "$quizId, $lessonId, $topic, $category, $from")

        startQuizButton.setOnClickListener {
            findNavController().navigate(QuizDetailFragmentDirections.actionQuizDetailFragmentToQuizFragment(quizId!!, lessonId!!, topic!!, category!!, from!!))
        }

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
