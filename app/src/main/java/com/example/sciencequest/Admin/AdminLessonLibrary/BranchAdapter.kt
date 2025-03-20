package com.example.sciencequest.Admin.AdminLessonLibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class BranchAdapter(
    private val branches: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<BranchAdapter.BranchViewHolder>() {

    inner class BranchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textViewBranch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BranchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_branch, parent, false)
        return BranchViewHolder(view)
    }

    override fun onBindViewHolder(holder: BranchViewHolder, position: Int) {
        val branch = branches[position]
        holder.textView.text = branch
        holder.itemView.setOnClickListener { onItemClick(branch) }
    }

    override fun getItemCount(): Int = branches.size
}
