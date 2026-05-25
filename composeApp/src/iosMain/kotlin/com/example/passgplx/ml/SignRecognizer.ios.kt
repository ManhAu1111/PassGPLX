package com.example.passgplx.ml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class SignRecognizer {
    actual fun recognize(imageByteArray: ByteArray): SignRecognitionResult? {
        // Dummy implementation for iOS
        return null
    }
}

@Composable
actual fun rememberSignRecognizer(): SignRecognizer {
    return remember { SignRecognizer() }
}
