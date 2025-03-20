package com.example.sciencequest.Admin.AdminQuiz

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.sciencequest.R
import com.google.android.material.textfield.TextInputEditText

class AdminAddQuestionDialogFragment : DialogFragment() {

    private var editMode = false
    private var adminQuestion: AdminQuestion? = null
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialogTheme)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_admin_add_question, null)


        adminQuestion = arguments?.getParcelable("question")
        position = arguments?.getInt("position") ?: -1
        editMode = (adminQuestion != null)

        val questionInput = view.findViewById<TextInputEditText>(R.id.editTextQuestion)
        val radioGroupQuestionType = view.findViewById<RadioGroup>(R.id.radioGroupQuestionType)
        val layoutMultipleChoiceOptions = view.findViewById<LinearLayout>(R.id.layoutMultipleChoiceOptions)
        val correctAnswerInput = view.findViewById<TextInputEditText>(R.id.editTextCorrectAnswer)
        val buttonAddQuestion = view.findViewById<Button>(R.id.buttonAddQuestion)

        radioGroupQuestionType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonMultipleChoice -> layoutMultipleChoiceOptions.visibility = View.VISIBLE
                R.id.radioButtonTrueFalse -> layoutMultipleChoiceOptions.visibility = View.GONE
            }
        }

        if (editMode) {
            adminQuestion?.let {
                questionInput.setText(it.question)
                correctAnswerInput.setText(it.correct)
                if (it.options.size > 2) {
                    radioGroupQuestionType.check(R.id.radioButtonMultipleChoice)
                    view.findViewById<TextInputEditText>(R.id.editTextOption1).setText(it.options[0])
                    view.findViewById<TextInputEditText>(R.id.editTextOption2).setText(it.options[1])
                    view.findViewById<TextInputEditText>(R.id.editTextOption3).setText(it.options[2])
                    view.findViewById<TextInputEditText>(R.id.editTextOption4).setText(it.options[3])
                } else {
                    radioGroupQuestionType.check(R.id.radioButtonTrueFalse)
                }
            }
        }

        buttonAddQuestion.setOnClickListener {
            val questionText = questionInput.text.toString()
            val correctAnswer = correctAnswerInput.text.toString()

            val adminQuestion = if (radioGroupQuestionType.checkedRadioButtonId == R.id.radioButtonMultipleChoice) {
                val option1 = view.findViewById<TextInputEditText>(R.id.editTextOption1).text.toString()
                val option2 = view.findViewById<TextInputEditText>(R.id.editTextOption2).text.toString()
                val option3 = view.findViewById<TextInputEditText>(R.id.editTextOption3).text.toString()
                val option4 = view.findViewById<TextInputEditText>(R.id.editTextOption4).text.toString()
                AdminQuestion(questionText, correctAnswer, listOf(option1, option2, option3, option4))
            } else {
                AdminQuestion(questionText, correctAnswer, listOf("True", "False"))
            }

            val targetFragment = targetFragment as? AdminQuizDetailFragment
            if (editMode && position != -1) {
                targetFragment?.editQuestion(adminQuestion, position)
            } else {
                targetFragment?.addQuestion(adminQuestion)
            }
            dismiss()
        }

        val dialog = builder.setView(view).create()

        // Apply rounded corners
        dialog.window?.setBackgroundDrawableResource(R.drawable.white_bg)

        // Return the actual dialog, not a new one
        return dialog

    }

    companion object {
        fun newInstance(adminQuestion: AdminQuestion? = null, position: Int = -1): AdminAddQuestionDialogFragment {
            val fragment = AdminAddQuestionDialogFragment()
            val args = Bundle()
            args.putParcelable("question", adminQuestion)
            args.putInt("position", position)
            fragment.arguments = args
            return fragment
        }
    }
}
