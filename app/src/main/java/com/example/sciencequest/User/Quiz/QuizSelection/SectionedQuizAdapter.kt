package com.example.sciencequest.User.Quiz.QuizSelection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class SectionedQuizAdapter(
    private var topicLessons: Map<String, List<Lesson>>,
    private val onQuizClick: (String, String, String, String) -> Unit  // Updated callback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_TOPIC = 0
    private val VIEW_TYPE_QUIZ = 1

    private val items = mutableListOf<Item>()

    init {
        updateItems()
    }

    private fun updateItems() {
        items.clear()
        topicLessons.forEach { (topic, lessons) ->
            items.add(Item.TopicItem(topic))
            lessons.forEach { lesson ->
                items.add(Item.QuizItem(lesson))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Item.TopicItem -> VIEW_TYPE_TOPIC
            is Item.QuizItem -> VIEW_TYPE_QUIZ
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_TOPIC) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_quiz_topic, parent, false)
            TopicViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_quiz, parent, false)
            QuizViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TopicViewHolder) {
            holder.bind((items[position] as Item.TopicItem).topic)
        } else if (holder is QuizViewHolder) {
            val lesson = (items[position] as Item.QuizItem).lesson
            holder.bind(lesson)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class TopicViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val topicTitle: TextView = view.findViewById(R.id.topicTitle)

        fun bind(topic: String) {
            topicTitle.text = topic
        }
    }

    inner class QuizViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val quizTitle: TextView = view.findViewById(R.id.quizTitle)
        private val quizDescription: TextView = view.findViewById(R.id.quizDescription)
        private val quizQuestions: TextView = view.findViewById(R.id.quizQuestions)
        private val startQuizButton: Button = view.findViewById(R.id.startQuizButton)

        fun bind(lesson: Lesson) {
            quizTitle.text = lesson.quiz?.title
            quizDescription.text = lesson.description
            quizQuestions.text = "Questions: 10"  // Replace with actual data

            startQuizButton.setOnClickListener {
                lesson.quiz?.quizId?.let { quizId ->
                    onQuizClick(quizId, lesson.lessonId, lesson.category, lesson.topic)
                }
            }
        }
    }

    sealed class Item {
        data class TopicItem(val topic: String) : Item()
        data class QuizItem(val lesson: Lesson) : Item()
    }

    fun updateData(newTopicLessons: Map<String, List<Lesson>>) {
        topicLessons = newTopicLessons
        updateItems()
        notifyDataSetChanged()
    }
}
