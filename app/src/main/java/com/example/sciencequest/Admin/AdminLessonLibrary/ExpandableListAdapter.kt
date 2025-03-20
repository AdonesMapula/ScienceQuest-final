package com.example.sciencequest.Admin.AdminLessonLibrary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.sciencequest.R

class ExpandableListAdapter(
    private val context: Context,
    private val topics: List<String>,
    private val lessons: Map<String, List<Map<String, String>>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = topics.size

    override fun getChildrenCount(groupPosition: Int): Int {
        return lessons[topics[groupPosition]]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any {
        return topics[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        // Ensure the lesson is returned as a Map
        return lessons[topics[groupPosition]]?.get(childPosition) ?: emptyMap<String, String>()
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val topic = getGroup(groupPosition) as String
        val view = LayoutInflater.from(context).inflate(R.layout.custom_spinner_item, parent, false)
        (view as TextView).text = topic
        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val lesson = getChild(groupPosition, childPosition) as Map<String, String>
        val lessonTitle = lesson["lessonTitle"] ?: ""
        val view = LayoutInflater.from(context).inflate(R.layout.item_small_spinner, parent, false)
        (view as TextView).text = lessonTitle
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
