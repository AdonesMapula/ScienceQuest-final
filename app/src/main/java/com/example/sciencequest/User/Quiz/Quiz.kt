package com.example.sciencequest.User.Quiz

data class Quiz(
    val title: String = "",
    val description: String = "",
    val questions: List<Question> = listOf()  // Corrected to match Firebase structure
)

data class Question(
    val question: String = "",
    val options: List<String> = listOf(),
    val correct: String = ""
)
