package com.example.passgplx.ml

import androidx.compose.runtime.Composable

expect class SignRecognizer {
    fun recognize(imageByteArray: ByteArray): SignRecognitionResult?
}

data class SignRecognitionResult(
    val label: String,
    val confidence: Float
)

@Composable
expect fun rememberSignRecognizer(): SignRecognizer
