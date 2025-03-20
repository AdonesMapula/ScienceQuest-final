package com.example.sciencequest.User.Quiz.QuizSelection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class QuizAdapter(
    private var lessons: List<Lesson>,
    private val onQuizClick: (String) -> Unit
) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.bind(lesson)
    }

    override fun getItemCount(): Int = lessons.size

    fun updateData(newLessons: List<Lesson>) {
        lessons = newLessons
        notifyDataSetChanged()
    }

    inner class QuizViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val quizTitle: TextView = view.findViewById(R.id.quizTitle)
        private val quizDescription: TextView = view.findViewById(R.id.quizDescription)

        private val quizQuestions: TextView = view.findViewById(R.id.quizQuestions)

        private val startQuizButton: Button = view.findViewById(R.id.startQuizButton)

        fun bind(lesson: Lesson) {
            quizTitle.text = lesson.quiz?.title
            quizDescription.text = lesson.description

            quizQuestions.text = "Questions: 10"        // Replace with actual data

            startQuizButton.setOnClickListener {
                lesson.quiz?.quizId?.let { onQuizClick(it) }
            }
        }
    }
}
