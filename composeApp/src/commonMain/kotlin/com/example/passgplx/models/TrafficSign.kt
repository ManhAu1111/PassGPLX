package com.example.passgplx.models

import kotlinx.serialization.Serializable

@Serializable
data class TrafficSign(
    val id: String,
    val code: String,
    val name: String,
    val type: String,
    val category: String,
    val detail: String,
    val image: String
)
