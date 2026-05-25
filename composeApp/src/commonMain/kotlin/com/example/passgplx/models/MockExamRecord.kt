package com.example.passgplx.models

import kotlinx.serialization.Serializable

@Serializable
data class MockExamRecord(
    val id: String,
    val timestamp: Long,
    val data: MockExamStateData
)
