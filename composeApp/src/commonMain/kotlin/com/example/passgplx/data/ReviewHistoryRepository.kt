package com.example.passgplx.data

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import com.example.passgplx.models.MockExamRecord

class ReviewHistoryRepository {
    private val settings: Settings = Settings()
    private val HISTORY_KEY = "review_history"

    fun getSavedAnswers(): Map<String, String> {
        val jsonString = settings.getString(HISTORY_KEY, "")
        if (jsonString.isEmpty()) {
            return emptyMap()
        }
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveAnswers(answers: Map<String, String>) {
        settings.putString(HISTORY_KEY, Json.encodeToString(answers))
    }

    fun saveAnswer(questionId: String, answerId: String) {
        val currentAnswers = getSavedAnswers().toMutableMap()
        currentAnswers[questionId] = answerId
        saveAnswers(currentAnswers)
    }

    private val MOCK_EXAM_KEY = "mock_exam_state"

    fun getSavedMockExam(): String? {
        val json = settings.getString(MOCK_EXAM_KEY, "")
        return if (json.isNotEmpty()) json else null
    }

    fun saveMockExam(json: String) {
        settings.putString(MOCK_EXAM_KEY, json)
    }

    fun clearMockExam() {
        settings.remove(MOCK_EXAM_KEY)
    }

    private val MOCK_EXAM_HISTORY_KEY = "mock_exam_history"

    fun getCompletedMockExams(): List<MockExamRecord> {
        val json = settings.getString(MOCK_EXAM_HISTORY_KEY, "")
        if (json.isEmpty()) return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addCompletedMockExam(record: MockExamRecord) {
        val currentHistory = getCompletedMockExams().toMutableList()
        currentHistory.add(0, record) // add to top
        settings.putString(MOCK_EXAM_HISTORY_KEY, Json.encodeToString(currentHistory))
    }
}
