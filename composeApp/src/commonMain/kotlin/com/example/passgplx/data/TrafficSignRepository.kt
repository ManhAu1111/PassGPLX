package com.example.passgplx.data

import com.example.passgplx.models.TrafficSign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import passgplx.composeapp.generated.resources.Res

/**
 * Singleton repository cho danh sách biển báo giao thông.
 * Cache data.json sau lần đọc đầu tiên để tránh parse lặp lại
 * từ TrafficSignsScreen và SignDetectionScreen.
 */
object TrafficSignRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private var cached: List<TrafficSign>? = null

    @OptIn(ExperimentalResourceApi::class)
    suspend fun getSigns(): List<TrafficSign> {
        cached?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val bytes = Res.readBytes("files/data.json")
                val jsonString = bytes.decodeToString()
                val signs = json.decodeFromString<List<TrafficSign>>(jsonString)
                cached = signs
                signs
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}
