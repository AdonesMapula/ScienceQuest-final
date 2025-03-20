package com.example.sciencequest.User.Quiz.QuizSelection

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class QuizSelectionFragment : Fragment() {

    private val viewModel: QuizSelectionViewModel by viewModels()
    private lateinit var adapter: SectionedQuizAdapter
    private var allLessons: Map<String, List<Lesson>> = emptyMap()
    private var filteredLessons: Map<String, List<Lesson>> = emptyMap()
    private lateinit var filterIcon: ImageView
    private lateinit var searchBar: EditText
    private lateinit var chemistryBtn: Button
    private lateinit var physicsBtn: Button
    private lateinit var earthBtn: Button
    private lateinit var mathBtn: Button
    private lateinit var biologyBtn: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        searchBar = view.findViewById(R.id.searchBar)
        filterIcon = view.findViewById(R.id.filterIcon)

        chemistryBtn= view.findViewById(R.id.startChemistry)
        physicsBtn= view.findViewById(R.id.startPhysics)
        earthBtn= view.findViewById(R.id.startEarth)
        mathBtn= view.findViewById(R.id.startMath)
        biologyBtn= view.findViewById(R.id.startBiology)



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
//            filteredLessons = lessons
//            adapter = SectionedQuizAdapter(filteredLessons) { quizId ->
//                val action = QuizSelectionFragmentDirections.actionQuizSelectionFragmentToQuizDetailFragment(quizId)
//                findNavController().navigate(action)
//            }
//            recyclerView.adapter = adapter
//        }

        viewModel.fetchLessons { lessons ->
            allLessons = lessons
            filteredLessons = allLessons.filterKeys { it == selectedFilter || selectedFilter == "All" }

            adapter = SectionedQuizAdapter(filteredLessons) { quizId, lessonId, category, topic ->
                val action = QuizzesFragmentDirections.actionQuizzesFragmentToQuizDetailFragment(
                    quizId, lessonId, topic, category, "quizzes"
                )
                findNavController().navigate(action)
            }
            recyclerView.adapter = adapter
        }


        chemistryBtn.setOnClickListener{
            findNavController().navigate(QuizSelectionFragmentDirections.actionQuizSelectionFragmentToQuizzesFragment("Chemistry"))
        }
        physicsBtn.setOnClickListener{
            findNavController().navigate(QuizSelectionFragmentDirections.actionQuizSelectionFragmentToQuizzesFragment("Physics"))
        }
        earthBtn.setOnClickListener{
            findNavController().navigate(QuizSelectionFragmentDirections.actionQuizSelectionFragmentToQuizzesFragment("Earth"))
        }
        mathBtn.setOnClickListener{
            findNavController().navigate(QuizSelectionFragmentDirections.actionQuizSelectionFragmentToQuizzesFragment("Math"))
        }
        biologyBtn.setOnClickListener{
            findNavController().navigate(QuizSelectionFragmentDirections.actionQuizSelectionFragmentToQuizzesFragment("Biology"))
        }


    }

    private var selectedFilter: String = "All"

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
