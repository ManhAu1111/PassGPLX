package com.example.passgplx.data

import com.example.passgplx.models.Question
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import passgplx.composeapp.generated.resources.Res

object QuestionRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedQuestions: List<Question>? = null

    @OptIn(ExperimentalResourceApi::class)
    suspend fun getAllQuestions(): List<Question> {
        if (cachedQuestions != null) {
            return cachedQuestions!!
        }
        return withContext(Dispatchers.IO) {
            try {
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
    }

    suspend fun getRandomQuestions(licenseType: com.example.passgplx.models.LicenseType): List<Question> {
        val all = getAllQuestions().filter { it.licenseClasses.contains(licenseType.name) }
        return if (all.size <= licenseType.totalMockQuestions) all else all.shuffled().take(licenseType.totalMockQuestions)
    }
}
