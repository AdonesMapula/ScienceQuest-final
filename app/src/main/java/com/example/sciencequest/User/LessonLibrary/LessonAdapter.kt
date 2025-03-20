package com.example.sciencequest.User.LessonLibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class LessonAdapter(
    private val lessons: List<Lesson>,
    private val itemBackgroundColor: Int,
    private val onItemClick: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.bind(lesson)
    }

    override fun getItemCount(): Int = lessons.size

    inner class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.lessonTitle)
        //private val descriptionTextView: TextView = itemView.findViewById(R.id.lessonDescription)
        private val progressTextView: TextView = itemView.findViewById(R.id.lessonProgress)
        private val itemLessonContainer: LinearLayout = itemView.findViewById(R.id.itemLessonContainer)

        fun bind(lesson: Lesson) {
            titleTextView.text = lesson.title
            //descriptionTextView.text = lesson.description
            itemView.setOnClickListener { onItemClick(lesson) }

            // Assuming `lesson.progress` contains the user's progress percentage
            progressTextView.text = "Completed (${lesson.progress}%)"

            // Change background color of the container dynamically
            itemLessonContainer.setBackgroundColor(itemView.context.getColor(itemBackgroundColor))

        }

    }
}

