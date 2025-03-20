package com.example.sciencequest.User.Quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.sciencequest.R
import com.example.sciencequest.databinding.FragmentQuizSettingsBinding

class QuizSettingsFragment : Fragment(R.layout.fragment_quiz_settings) {

    private var _binding: FragmentQuizSettingsBinding? = null
    private val binding get() = _binding!!
    private val args: QuizSettingsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuizSettingsBinding.inflate(inflater, container, false)

        // Display current progress
        val progressPercentage = ((args.currentProgress.toFloat() / args.totalQuestions) * 100).toInt()
        binding.quizProgressPercentage.text = "$progressPercentage% (${args.currentProgress} of ${args.totalQuestions})"

        // Apply progress on progress bar
        binding.quizProgressBar.max = args.totalQuestions
        binding.quizProgressBar.progress = args.currentProgress

        // Resume button click listener
        binding.resumeButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Exit button click listener with confirmation dialog
        binding.exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }

        return binding.root
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)


        // Inflate the custom view
        val customView = layoutInflater.inflate(R.layout.quiz_exit_confirmation_dialog, null)
        builder.setView(customView)

        // Create and show the dialog
        val dialog = builder.create()
        dialog.show()

        // Find and customize buttons within the custom view
        customView.findViewById<Button>(R.id.positiveButton).apply {

            setOnClickListener {
                findNavController().popBackStack(R.id.lessonDetailsFragment, false)
                dialog.dismiss()
            }
        }

        customView.findViewById<Button>(R.id.negativeButton).apply {

            setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
