package com.example.sciencequest.User.LessonLibrary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class TopicAdapter(
    private val topics: List<String>,
    private val itemBackgroundColor: Int,
    private val onTopicClick: (String) -> Unit
) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    class TopicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.topicTitle)
        val topicContainer: LinearLayout = view.findViewById(R.id.topicContainer)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topic, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]
        holder.title.text = topic
        holder.itemView.setOnClickListener { onTopicClick(topic) }

        // Correct way to set background color using ContextCompat
        val context: Context = holder.itemView.context
        holder.topicContainer.setBackgroundColor(ContextCompat.getColor(context, itemBackgroundColor))

    }

    override fun getItemCount(): Int = topics.size
}
