package com.example.sciencequest.User.Quiz

// QuizViewModel.kt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {
    private val _quizData = MutableLiveData<Quiz>()
    val quizData: LiveData<Quiz> get() = _quizData

    fun setQuizData(quiz: Quiz) {
        _quizData.value = quiz
    }

//    fun submitAnswer(selectedOption: Int) {
//        val correct = _quizData.value?.correctAnswer == selectedOption
//        // Handle result (e.g., update UI or save to Firebase)
//    }
}
