package com.example.passgplx.models

import kotlinx.serialization.Serializable

@Serializable
data class MockExamStateData(
    val licenseType: String,
    val questions: List<Question>,
    val selectedAnswers: Map<String, String>,
    val timeRemainingSeconds: Int,
    val currentIndex: Int,
    val isSubmitted: Boolean,
    val score: Int
)
