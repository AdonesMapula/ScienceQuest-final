package com.example.sciencequest.Admin.AdminQuiz

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdminQuestion(
    val question: String = "",
    val correct: String = "",
    val options: List<String> = emptyList()
) : Parcelable
