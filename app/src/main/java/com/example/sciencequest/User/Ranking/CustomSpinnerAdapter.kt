package com.example.sciencequest.User.Ranking

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.sciencequest.R

class CustomSpinnerAdapter(
    context: Context,
    private val dataList: List<String>
) : ArrayAdapter<String>(context, R.layout.spinner_item_layout, dataList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        // Customize the text appearance for the selected item (e.g., font size, color)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Correct: Change text color
        textView.textSize = 14f // Example: Change text size
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        // Customize the dropdown view
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Correct: Use getColor for resources
        textView.textSize = 14f // Example: Change dropdown item text size
        view.setBackgroundColor(Color.parseColor("#F2C94C")) // Example: Set background color for each item
        return view
    }
}
