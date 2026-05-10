package com.example.passgplx

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform