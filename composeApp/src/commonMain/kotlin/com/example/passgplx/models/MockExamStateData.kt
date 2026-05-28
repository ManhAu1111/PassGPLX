package com.example.passgplx.models

import kotlinx.serialization.Serializable
import com.example.passgplx.data.QuestionDataHelper

@Serializable
data class MockExamStateData(
    val licenseType: String,
    val questions: List<Question>,
    val selectedAnswers: Map<String, String>,
    val timeRemainingSeconds: Int,
    val currentIndex: Int,
    val isSubmitted: Boolean,
    val score: Int
) {
    fun isPassed(passingScore: Int): Boolean {
        val passedScore = this.score >= passingScore
        return passedScore && !hasFailedParalyzing()
    }

    fun hasFailedParalyzing(): Boolean {
        return this.questions.any { q ->
            if (QuestionDataHelper.paralyzingQuestionIds.contains(q.id)) {
                val selected = this.selectedAnswers[q.id]
                val correct = q.answers.find { it.correct }?.id
                selected != correct
            } else {
                false
            }
        }
    }
}
