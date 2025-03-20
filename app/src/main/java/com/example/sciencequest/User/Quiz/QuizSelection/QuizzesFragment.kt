package com.example.sciencequest.User.Quiz.QuizSelection

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R

class QuizzesFragment : Fragment() {

    private val viewModel: QuizSelectionViewModel by viewModels()
    private lateinit var adapter: SectionedQuizAdapter
    private var allLessons: Map<String, List<Lesson>> = emptyMap()
    private var filteredLessons: Map<String, List<Lesson>> = emptyMap()
    private lateinit var filterIcon: ImageView
    private lateinit var searchBar: EditText
    private lateinit var routeFilter: String
    private var selectedFilter: String = "All"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_biology_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        routeFilter = arguments?.getString("selectedFilter") ?: "All" // Set a default value
        selectedFilter = routeFilter

        Log.d("routeFilter", routeFilter)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        searchBar = view.findViewById(R.id.searchBar)
        filterIcon = view.findViewById(R.id.filterIcon)


        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set up filter icon click listener
        filterIcon.setOnClickListener {
            showFilterMenu(it)
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilterAndSearch(selectedFilter, s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

//        viewModel.fetchLessons { lessons ->
//            allLessons = lessons
//            filteredLessons = allLessons.filterKeys { it == selectedFilter || selectedFilter == "All" }
//
//            adapter = SectionedQuizAdapter(filteredLessons) { quizId ->
//                val action = QuizzesFragmentDirections.actionQuizzesFragmentToQuizDetailFragment(quizId)
//                findNavController().navigate(action)
//            }
//            recyclerView.adapter = adapter
//        }

        viewModel.fetchLessons { lessons ->
            allLessons = lessons
            filteredLessons = allLessons.filterKeys { it == selectedFilter || selectedFilter == "All" }

            adapter = SectionedQuizAdapter(filteredLessons) { quizId, lessonId, category, topic ->
                Log.d("QuizData", "$quizId, $lessonId, $topic, $category")
                val action = QuizzesFragmentDirections.actionQuizzesFragmentToQuizDetailFragment(
                    quizId, lessonId, topic, category, "quizzes"
                )
                findNavController().navigate(action)
            }
            recyclerView.adapter = adapter
        }



    }


    private fun showFilterMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val menuInflater = popupMenu.menuInflater
        menuInflater.inflate(R.menu.filter_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            selectedFilter = menuItem.title.toString()
            applyFilterAndSearch(selectedFilter, searchBar.text.toString())
            true
        }

        // Show the popup
        popupMenu.show()
    }

    private fun applyFilterAndSearch(filter: String, searchText: String) {
        filteredLessons = if (filter == "All") {
            allLessons
        } else {
            allLessons.filterKeys { it == filter }
        }

        if (searchText.isNotEmpty()) {
            filteredLessons = filteredLessons.mapValues { entry ->
                entry.value.filter {
                    it.title.contains(searchText, ignoreCase = true) ||
                            it.description.contains(searchText, ignoreCase = true)
                }
            }.filterValues { it.isNotEmpty() }
        }

        adapter.updateData(filteredLessons)
    }
}
