package com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class LessonIconAdapter(
    private var lessons: List<Lesson>,
    private var activeLessonId: String,
    private val onLessonClick: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonIconAdapter.LessonViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lesson_icon, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]


        // Set icon based on lesson type (using lessonType from data class)
        val iconRes = when (lesson.lessonType) {
            LessonType.VIDEO -> R.drawable.ic_video
            LessonType.SIMULATION -> R.drawable.ic_simulation
            LessonType.QUEST -> R.drawable.ic_quest
        }

        holder.lessonIcon.setImageResource(iconRes)

        // Apply color based on status
        val statusColor = when (lesson.lessonType) {
            LessonType.VIDEO -> {
                when (lesson.videoStatus) {
                    LessonStatus.COMPLETED -> R.color.cyan600
                    LessonStatus.UNTOUCHED -> R.color.disabledGray
                }
            }
            LessonType.SIMULATION -> {
                when (lesson.simulationStatus) {
                    LessonStatus.COMPLETED -> R.color.cyan600
                    LessonStatus.UNTOUCHED -> R.color.disabledGray
                }
            }
            LessonType.QUEST -> {
                when (lesson.quizStatus) {
                    LessonStatus.COMPLETED -> R.color.cyan600
                    LessonStatus.UNTOUCHED -> R.color.disabledGray
                }
            }
        }


        // Apply the color filter
        holder.lessonIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, statusColor))



        // Display the appropriate title and other content
        when (lesson.lessonType) {
            LessonType.VIDEO -> {
                holder.titleTv.text = lesson.title
                // You can also add the video URL or adjust the UI based on other details for the video
            }
            LessonType.SIMULATION -> {
                holder.titleTv.text = lesson.phetSimulation.title
                // Handle simulation-specific logic if needed, like showing a simulation link
            }
            LessonType.QUEST -> {
                holder.titleTv.text = lesson.quiz.title
                // Handle quiz-specific logic if needed, like showing a quiz ID or other details
            }
        }

        if (lesson.id == activeLessonId) {
            holder.borderLayout.setBackgroundResource(R.color.cyan500)
            holder.lineActive.visibility = View.VISIBLE
        } else {
            holder.borderLayout.setBackgroundResource(0) // Remove active border when not active
            holder.lineActive.visibility = View.GONE
        }


        // Set click listener for the lesson
        holder.itemView.setOnClickListener { onLessonClick(lesson) }
    }

    override fun getItemCount(): Int = lessons.size

    class LessonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lessonIcon: ImageView = view.findViewById(R.id.lessonIcon)
        val borderLayout: View = view.findViewById(R.id.borderLayout) // Border container
        val titleTv: TextView = view.findViewById(R.id.titleTv)
        val lineActive: View = view.findViewById(R.id.lineActive)
    }

    // Function to update the active lesson dynamically (you can pass a lesson ID to mark it as active)
    fun updateActiveLesson(newLessonId: String) {
        // Update the lesson status or change active lessons dynamically based on lesson ID
        notifyDataSetChanged() // Refresh the view after updating the active lesson
    }
}
