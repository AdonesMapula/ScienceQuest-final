package com.example.sciencequest.Admin.AdminQuiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class AdminQuestionAdapter(
    private val adminQuestions: List<AdminQuestion>,
    private val onEditClickListener: (AdminQuestion, Int) -> Unit,
    private val onDeleteClickListener: (Int) -> Unit
) : RecyclerView.Adapter<AdminQuestionAdapter.QuestionViewHolder>() {

    class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionText: TextView = view.findViewById(R.id.textViewQuestion)
        val optionsText: TextView = view.findViewById(R.id.textViewOptions)
        val deleteButton: Button = view.findViewById(R.id.buttonDeleteQuestion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = adminQuestions[position]
        holder.questionText.text = question.question
        holder.optionsText.text = "Options: ${question.options.joinToString(", ")}"

        holder.itemView.setOnClickListener { onEditClickListener(question, position) }
        holder.deleteButton.setOnClickListener { onDeleteClickListener(position) }
    }

    override fun getItemCount(): Int = adminQuestions.size
}
