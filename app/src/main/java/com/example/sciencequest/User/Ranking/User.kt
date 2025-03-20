package com.example.sciencequest.User.Ranking

data class User(
    val email: String = "",
    val username: String = "",
    val totalScore: Int? = null, // Total score across all categories
    val streak: Int = 0,
    val userId: String = "",
    val role: String = "",
    val categoryTotalScore: Map<String, Int>? = null,  // This is a map where each category score is stored
    val lessonProgress: Map<String, Map<String, LessonProgress>>? = null // Assuming lessonProgress is structured this way
)

data class LessonProgress(
    val quizScore: Int = 0,
    val quizStatus: String = ""
)