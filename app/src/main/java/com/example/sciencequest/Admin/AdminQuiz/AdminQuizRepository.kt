package com.example.sciencequest.Admin.AdminQuiz

import android.util.Log
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference

class AdminQuizRepository(private val database: DatabaseReference) {

    fun createQuiz(adminQuiz: AdminQuiz, onComplete: (Boolean) -> Unit) {
        val key = database.child("Quizzes").push().key
        if (key != null) {
            val quizWithId = adminQuiz.copy(id = key)
            database.child("Quizzes").child(key).setValue(quizWithId)
                .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
        } else {
            onComplete(false)
        }
    }

    fun readQuizzes(onComplete: (List<AdminQuiz>) -> Unit) {
        database.child("Quizzes").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val quizzes = task.result.children.mapNotNull { snapshot ->
                    try {
                        val data = snapshot.getValue(AdminQuiz::class.java)
                        Log.d("QuizRepository", "Retrieved quiz: $data")
                        data
                    } catch (e: DatabaseException) {
                        Log.e("QuizRepository", "Error retrieving quiz: ${snapshot.key} - ${snapshot.value}", e)
                        null
                    }
                }
                onComplete(quizzes)
            } else {
                onComplete(emptyList())
            }
        }
    }

    fun updateQuiz(quizId: String, adminQuiz: AdminQuiz, onComplete: (Boolean) -> Unit) {
        database.child("Quizzes").child(quizId).setValue(adminQuiz)
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
    }

    fun deleteQuiz(quizId: String, onComplete: (Boolean) -> Unit) {
        database.child("Quizzes").child(quizId).removeValue()
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
    }
}
