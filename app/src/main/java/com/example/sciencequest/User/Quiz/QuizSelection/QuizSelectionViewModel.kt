package com.example.sciencequest.User.Quiz.QuizSelection

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

//class QuizSelectionViewModel : ViewModel() {
//
//    fun fetchLessons(callback: (Map<String, List<Lesson>>) -> Unit) {
//        val database = FirebaseDatabase.getInstance().reference
//        val lessonsRef = database.child("Lessons")
//
//        lessonsRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val lessons = mutableMapOf<String, List<Lesson>>()
//                for (fieldSnapshot in snapshot.children) {
//                    val field = fieldSnapshot.key ?: continue
//                    val lessonList = mutableListOf<Lesson>()
//                    for (lessonSnapshot in fieldSnapshot.children) {
//                        for (topicSnapshot in lessonSnapshot.children) {
//                            val lesson = topicSnapshot.getValue(Lesson::class.java)
//                            lesson?.let { lessonList.add(it) }
//                        }
//                    }
//                    lessons[field] = lessonList
//                }
//                callback(lessons)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle error
//            }
//        })
//    }
//}
class QuizSelectionViewModel : ViewModel() {

    fun fetchLessons(callback: (Map<String, List<Lesson>>) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        val lessonsRef = database.child("Lessons")

        lessonsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lessons = mutableMapOf<String, List<Lesson>>()

                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.key ?: continue
                    val lessonList = mutableListOf<Lesson>()

                    for (topicSnapshot in categorySnapshot.children) {
                        val topic = topicSnapshot.key ?: continue

                        // 🔥 Skip non-lesson fields
                        if (topic == "fieldDescription" || topic == "fieldImageUrl" || topic == "fieldName" || topic == "topicDesc") {
                            continue
                        }

                        for (lessonSnapshot in topicSnapshot.children) {
                            val lessonId = lessonSnapshot.key ?: continue
                            try {
                                val lesson = lessonSnapshot.getValue(Lesson::class.java)?.copy(
                                    lessonId = lessonId,
                                    category = category,
                                    topic = topic
                                )
                                lesson?.let { lessonList.add(it) }
                            } catch (e: Exception) {
                                // 🔴 Log error for debugging
                                Log.e("Firebase", "Error parsing lesson: $lessonId", e)
                            }
                        }
                    }
                    lessons[category] = lessonList
                }
                callback(lessons)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
            }
        })
    }
}
