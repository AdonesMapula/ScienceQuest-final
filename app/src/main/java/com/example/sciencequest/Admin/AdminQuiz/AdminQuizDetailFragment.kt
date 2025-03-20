package com.example.sciencequest.Admin.AdminQuiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminQuizDetailFragment : Fragment() {

    private lateinit var adminQuizRepository: AdminQuizRepository
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adminQuestionAdapter: AdminQuestionAdapter
    private var adminQuiz: AdminQuiz? = null
    private val adminQuestions = mutableListOf<AdminQuestion>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_quiz_detail, container, false)

        database = FirebaseDatabase.getInstance().reference
        adminQuizRepository = AdminQuizRepository(database)

        adminQuiz = arguments?.getParcelable("quiz")

        val quizTitleInput = view.findViewById<EditText>(R.id.editTextQuizTitle)
        val quizDescriptionInput = view.findViewById<EditText>(R.id.editTextQuizDescription)
        val buttonAddQuestion = view.findViewById<Button>(R.id.buttonAddQuestion)
        val buttonSaveQuiz = view.findViewById<Button>(R.id.buttonSaveQuiz)
        recyclerView = view.findViewById(R.id.recyclerViewQuestions)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adminQuiz?.let {
            quizTitleInput.setText(it.title)
            quizDescriptionInput.setText(it.description)
            adminQuestions.addAll(it.questions)
        }

        adminQuestionAdapter = AdminQuestionAdapter(adminQuestions,
            onEditClickListener = { question, position -> showEditQuestionDialog(question, position) },
            onDeleteClickListener = { position -> deleteQuestion(position) }
        )
        recyclerView.adapter = adminQuestionAdapter

        buttonAddQuestion.setOnClickListener {
            showAddQuestionDialog()
        }

        buttonSaveQuiz.setOnClickListener {
            saveQuiz(quizTitleInput.text.toString(), quizDescriptionInput.text.toString())
        }

        return view
    }

    private fun showAddQuestionDialog() {

        val dialog = AdminAddQuestionDialogFragment.newInstance()
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "AddQuestionDialogFragment")
    }

    private fun showEditQuestionDialog(adminQuestion: AdminQuestion, position: Int) {
        val dialog = AdminAddQuestionDialogFragment.newInstance(adminQuestion, position)
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, "EditQuestionDialogFragment")
    }

    private fun saveQuiz(title: String, description: String) {
        val newAdminQuiz = AdminQuiz(title, description, adminQuestions)
        if (adminQuiz == null) {
            adminQuizRepository.createQuiz(newAdminQuiz) { success ->
                if (success) {
                    context?.let {
                        Toast.makeText(it, "Quiz created successfully", Toast.LENGTH_SHORT).show()
                    }
                    findNavController().popBackStack()
                } else {
                    context?.let {
                        Toast.makeText(it, "Failed to create quiz", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            val quizId = adminQuiz!!.id // Make sure this id is used in the update
            val updatedQuiz = newAdminQuiz.copy(id = quizId)
            adminQuizRepository.updateQuiz(quizId, updatedQuiz) { success ->
                if (success) {
                    context?.let {
                        Toast.makeText(it, "Quiz updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    findNavController().popBackStack()
                } else {
                    context?.let {
                        Toast.makeText(it, "Failed to update quiz", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun addQuestion(adminQuestion: AdminQuestion) {
        adminQuestions.add(adminQuestion)
        adminQuestionAdapter.notifyDataSetChanged()
    }

    fun editQuestion(adminQuestion: AdminQuestion, position: Int) {
        adminQuestions[position] = adminQuestion
        adminQuestionAdapter.notifyDataSetChanged()
    }

    private fun deleteQuestion(position: Int) {
        adminQuestions.removeAt(position)
        adminQuestionAdapter.notifyDataSetChanged()
    }
}

