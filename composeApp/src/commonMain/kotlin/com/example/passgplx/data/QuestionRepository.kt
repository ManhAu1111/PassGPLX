package com.example.passgplx.data

import com.example.passgplx.models.Question
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import passgplx.composeapp.generated.resources.Res

class QuestionRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedQuestions: List<Question>? = null

    @OptIn(ExperimentalResourceApi::class)
    suspend fun getAllQuestions(): List<Question> {
        if (cachedQuestions != null) {
            return cachedQuestions!!
        }
        return try {
            val bytes = Res.readBytes("files/questions.json")
            val jsonString = bytes.decodeToString()
            val questions = json.decodeFromString<List<Question>>(jsonString)
            cachedQuestions = questions
            questions
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getRandomQuestions(count: Int = 25): List<Question> {
        val all = getAllQuestions()
        return if (all.size <= count) all else all.shuffled().take(count)
    }
}
