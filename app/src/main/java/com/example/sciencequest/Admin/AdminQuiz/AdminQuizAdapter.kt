package com.example.sciencequest.Admin.AdminQuiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class AdminQuizAdapter(
    private val adminQuizs: List<AdminQuiz>,
    private val onQuizClickListener: (AdminQuiz) -> Unit,
    private val onEditClickListener: (AdminQuiz) -> Unit,
    private val onDeleteClickListener: (AdminQuiz) -> Unit
) : RecyclerView.Adapter<AdminQuizAdapter.QuizViewHolder>() {

    class QuizViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quizTitle: TextView = view.findViewById(R.id.textViewQuizTitle)
        val quizDescription: TextView = view.findViewById(R.id.textViewQuizDescription)
        val editButton: Button = view.findViewById(R.id.buttonEditQuiz)
        val deleteButton: Button = view.findViewById(R.id.buttonDeleteQuiz)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_quiz, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = adminQuizs[position]
        holder.quizTitle.text = quiz.title
        holder.quizDescription.text = quiz.description

        holder.itemView.setOnClickListener { onQuizClickListener(quiz) }
        holder.editButton.setOnClickListener { onEditClickListener(quiz) }
        holder.deleteButton.setOnClickListener { onDeleteClickListener(quiz) }
    }

    override fun getItemCount(): Int = adminQuizs.size
}
