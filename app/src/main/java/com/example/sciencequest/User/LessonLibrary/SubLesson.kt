package com.example.sciencequest.User.LessonLibrary

// Step 2: Lesson Data Class
//data class Lesson(
//    val id: String,
//    val title: String,
//    val description: String,
//    val thumbnailResource: Int,
//    val progress: Int,
//    val isLoading: Boolean = false  // Add loading state
//)

data class SubLesson(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailResource: Int,
    val progress: Int,
    val videoUrl: String? = null, // Add video URL field
    val isLoading: Boolean = false
)
