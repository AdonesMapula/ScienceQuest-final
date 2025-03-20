package com.example.sciencequest.User.Quiz.QuizSelection

data class Lesson(
    val lessonId: String = "",
    val category: String = "",
    val topic: String = "",
    val description: String = "",
    val isEnabled: Boolean = false,
    val lessonType: String = "",
    val phetSimulation: PhetSimulation? = null,
    val quiz: Quiz? = null,
    val title: String = "",
    val videoUrl: String = ""
)

data class PhetSimulation(
    val isEnabled: Boolean = false,
    val lessonType: String = "",
    val title: String = "",
    val url: String = ""
)

data class Quiz(
    val isEnabled: Boolean = false,
    val lessonType: String = "",
    val quizId: String = "",
    val title: String = ""
)
