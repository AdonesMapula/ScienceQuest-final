package com.example.sciencequest.User.LessonLibrary

data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    var progress: Int
)
