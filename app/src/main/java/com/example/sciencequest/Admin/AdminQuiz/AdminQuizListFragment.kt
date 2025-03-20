package com.example.sciencequest.Admin.AdminQuiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminQuizListFragment : Fragment() {

    private lateinit var adminQuizRepository: AdminQuizRepository
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adminQuizAdapter: AdminQuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_quiz_list, container, false)

        database = FirebaseDatabase.getInstance().reference
        adminQuizRepository = AdminQuizRepository(database)

        recyclerView = view.findViewById(R.id.recyclerViewQuizzes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val buttonAddQuiz = view.findViewById<ImageButton>(R.id.buttonAddQuiz)
        buttonAddQuiz.setOnClickListener {
            findNavController().navigate(R.id.action_quizListFragment_to_quizDetailFragment)
        }

        loadQuizzes()

        return view
    }

    private fun loadQuizzes() {
        adminQuizRepository.readQuizzes { quizzes ->
            if (quizzes.isNotEmpty()) {
                adminQuizAdapter = AdminQuizAdapter(
                    quizzes,
                    onQuizClickListener = { quiz ->
                        val action = AdminQuizListFragmentDirections.actionQuizListFragmentToQuizDetailFragment(quiz)
                        findNavController().navigate(action)
                    },
                    onEditClickListener = { quiz ->
                        val action = AdminQuizListFragmentDirections.actionQuizListFragmentToQuizDetailFragment(quiz)
                        findNavController().navigate(action)
                    },
                    onDeleteClickListener = { quiz -> deleteQuiz(quiz) }
                )
                recyclerView.adapter = adminQuizAdapter
            } else {
                // Handle empty state
                val noQuizzesTextView = view?.findViewById<TextView>(R.id.tv_no_quizzes)
                noQuizzesTextView?.visibility = View.VISIBLE
            }
        }
    }

    private fun deleteQuiz(adminQuiz: AdminQuiz) {
        adminQuizRepository.deleteQuiz(adminQuiz.id) { success ->
            if (success) {
                loadQuizzes() // Refresh the list after deletion
            } else {
                context?.let {
                    Toast.makeText(it, "Failed to delete quiz", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
