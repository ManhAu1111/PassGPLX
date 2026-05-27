package com.example.passgplx.models

enum class LicenseType(val displayName: String, val totalMockQuestions: Int, val passingScore: Int, val timeMinutes: Int) {
    B1("Hạng B1", 25, 21, 19),
    B("Hạng B", 30, 27, 20),
    C1("Hạng C1", 35, 32, 22),
    C("Hạng C", 40, 37, 24)
}
