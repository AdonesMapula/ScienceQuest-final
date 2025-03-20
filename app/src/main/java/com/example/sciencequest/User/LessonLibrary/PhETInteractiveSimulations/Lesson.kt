package com.example.sciencequest.User.LessonLibrary.PhETInteractiveSimulations

import java.util.UUID

//
//data class Lesson(
//    val id: String,
//    val title: String,
//    val type: LessonType,
//    var status: LessonStatus, // Completed, InProgress, Locked
//
//    var phetSimulationUrl: String,
//    var phetStatus: String,
//
//    var quizId: String,
//    var quizStatus: String
//)
//





data class Lesson(
    val id: String,
    val description: String,
    val title: String,
    val videoUrl: String,
    //val lessonStatus: LessonStatus, // completed, in_progress, untoched
    val lessonType: LessonType,
    val isEnabled: Boolean, // true or false
    val phetSimulation: Simulation,
    val quiz: Quiz,
    val videoStatus: LessonStatus,
    val simulationStatus: LessonStatus,
    val quizStatus: LessonStatus
)

data class Simulation(
    val title: String,
    val url: String,
    //val lessonStatus: LessonStatus, // completed, in_progress, untoched
    val lessonType: LessonType,
    val isEnabled: Boolean
)

data class Quiz(
    val title: String,
    val quizId: String,
    //val lessonStatus: LessonStatus, // completed, in_progress, untoched
    val lessonType: LessonType,
    val isEnabled: Boolean
)

enum class LessonType {
    //VIDEO, LESSON, SIMULATION, QUEST
    VIDEO, SIMULATION, QUEST
}

enum class LessonStatus {
    COMPLETED, UNTOUCHED
}


