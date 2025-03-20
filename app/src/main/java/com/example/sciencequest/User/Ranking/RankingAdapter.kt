package com.example.sciencequest.User.Ranking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class RankingAdapter(private var userList: List<User>, private var category: String) :
    RecyclerView.Adapter<RankingAdapter.UserViewHolder>() {

    // ViewHolder for regular user rows
    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        val totalScoreTextView: TextView = view.findViewById(R.id.totalScoreTextView)
        val rankTextView: TextView = view.findViewById(R.id.rankTextView)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return UserViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    // Use the appropriate medal for 4th and onwards (you can adjust this as needed)
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.usernameTextView.text = user.username

        // Display score based on category
        val score = when (category) {
            "Biology" -> user.categoryTotalScore?.get("Biology") ?: 0
            "Chemistry" -> user.categoryTotalScore?.get("Chemistry") ?: 0
            "Physics" -> user.categoryTotalScore?.get("Physics") ?: 0
            else -> user.totalScore ?: 0
        }

        holder.totalScoreTextView.text = "$score pts."

        // Set the rank for the user
        val rank = position + 4  // 4th place onwards
        holder.rankTextView.text = "$rank"

        // Set the default icon for regular users (not podium)
        //holder.rankIcon.setImageResource(R.drawable.user_icon)  // Change as needed
    }



    // Return the size of the dataset
    override fun getItemCount(): Int {
        return userList.size
    }

    // Update the data in the adapter
    fun updateData(newList: List<User>, category: String) {
        userList = newList
        this.category = category
        notifyDataSetChanged()
    }
}
