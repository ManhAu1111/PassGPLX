package com.example.passgplx.models

data class Category(
    val id: String,
    val name: String,
    val description: String,
    val isParalyzing: Boolean = false,
    val filter: (Question) -> Boolean
)
