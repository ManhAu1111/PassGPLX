package com.example.passgplx.models

import kotlinx.serialization.Serializable

@Serializable
data class Answer(
    val id: String,
    val text: String,
    val correct: Boolean
)

@Serializable
data class Question(
    val id: String,
    val question: String,
    val answers: List<Answer>,
    val image: String? = null,
    val licenseClasses: List<String> = emptyList()
)
