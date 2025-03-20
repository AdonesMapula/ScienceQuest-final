package com.example.sciencequest.Admin.AdminQuiz

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class AdminQuiz(
    val title: String = "",
    val description: String = "",
    val questions: List<AdminQuestion> = emptyList(),
    val id: String = ""  // Make sure this matches your Firebase data structure
) : Parcelable