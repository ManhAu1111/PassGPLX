package com.example.passgplx.models

enum class LicenseType(val displayName: String, val totalMockQuestions: Int, val passingScore: Int, val timeMinutes: Int) {
    A1("Hạng A1", 25, 21, 19),
    A("Hạng A", 25, 23, 19),
    B1("Hạng B1", 30, 27, 20),
    B("Hạng B", 35, 32, 22),
    C1("Hạng C1", 40, 36, 24),
    C("Hạng C", 40, 36, 24)
}
