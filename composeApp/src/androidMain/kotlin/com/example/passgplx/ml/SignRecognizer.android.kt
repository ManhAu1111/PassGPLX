package com.example.passgplx.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

actual class SignRecognizer(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var labelsMap: Map<String, String> = emptyMap()

    private var inputWidth: Int = 224
    private var inputHeight: Int = 224
    private var inputDataType: DataType = DataType.FLOAT32

    init {
        try {
            // Load labels
            val jsonString = context.assets.open("label_mapping.json").bufferedReader().use { it.readText() }
            val jsonElement = Json.parseToJsonElement(jsonString)
            val tempMap = mutableMapOf<String, String>()
            jsonElement.jsonObject.forEach { (key, value) ->
                tempMap[key] = value.jsonPrimitive.content
            }
            labelsMap = tempMap

            // Load model
            val assetFileDescriptor = context.assets.openFd("model_bien_bao.tflite")
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            val options = Interpreter.Options()
            options.setNumThreads(4)
            interpreter = Interpreter(buffer, options)

            // Extract input shape and type
            val inputTensor = interpreter?.getInputTensor(0)
            if (inputTensor != null) {
                val shape = inputTensor.shape()
                if (shape.size >= 3) {
                    inputHeight = shape[1]
                    inputWidth = shape[2]
                }
                inputDataType = inputTensor.dataType()
                Log.d("SignRecognizer", "Model input: shape=${shape.contentToString()}, type=${inputDataType}")
            }
        } catch (e: Exception) {
            Log.e("SignRecognizer", "Error initializing model or labels", e)
        }
    }

    actual fun recognize(imageByteArray: ByteArray): SignRecognitionResult? {
        val interp = interpreter ?: return null

        try {
            val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size) ?: return null
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)

            // Prepare input buffer
            val bytesPerChannel = if (inputDataType == DataType.FLOAT32) 4 else 1
            val inputBuffer = ByteBuffer.allocateDirect(1 * inputWidth * inputHeight * 3 * bytesPerChannel)
            inputBuffer.order(ByteOrder.nativeOrder())

            val intValues = IntArray(inputWidth * inputHeight)
            scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

            var pixel = 0
            for (i in 0 until inputHeight) {
                for (j in 0 until inputWidth) {
                    val `val` = intValues[pixel++]
                    val r = (`val` shr 16) and 0xFF
                    val g = (`val` shr 8) and 0xFF
                    val b = `val` and 0xFF

                    if (inputDataType == DataType.FLOAT32) {
                        // Normalize 0-255 to 0.0-1.0 (or -1 to 1 based on model, typically 0-1 or normalization is built-in)
                        // If standard MobileNet, it might be -1 to 1. We will use 0.0-1.0 by default, 
                        // if performance is bad we might need (val / 127.5f) - 1.0f
                        inputBuffer.putFloat(r / 255.0f)
                        inputBuffer.putFloat(g / 255.0f)
                        inputBuffer.putFloat(b / 255.0f)
                    } else {
                        inputBuffer.put(r.toByte())
                        inputBuffer.put(g.toByte())
                        inputBuffer.put(b.toByte())
                    }
                }
            }

            // Output buffer
            val outputTensor = interp.getOutputTensor(0)
            val outputShape = outputTensor.shape()
            val numClasses = outputShape[1]
            
            // Assume single output tensor with shape [1, numClasses]
            val outputDataType = outputTensor.dataType()
            
            var bestClassIndex = -1
            var bestConfidence = 0.0f

            if (outputDataType == DataType.FLOAT32) {
                val outputBuffer = Array(1) { FloatArray(numClasses) }
                interp.run(inputBuffer, outputBuffer)
                
                val confidences = outputBuffer[0]
                for (i in 0 until numClasses) {
                    if (confidences[i] > bestConfidence) {
                        bestConfidence = confidences[i]
                        bestClassIndex = i
                    }
                }
            } else if (outputDataType == DataType.UINT8) {
                val outputBuffer = Array(1) { ByteArray(numClasses) }
                interp.run(inputBuffer, outputBuffer)
                
                val confidences = outputBuffer[0]
                for (i in 0 until numClasses) {
                    val conf = (confidences[i].toInt() and 0xFF) / 255.0f
                    if (conf > bestConfidence) {
                        bestConfidence = conf
                        bestClassIndex = i
                    }
                }
            }

            if (bestClassIndex != -1) {
                val label = labelsMap[bestClassIndex.toString()] ?: "Unknown ($bestClassIndex)"
                return SignRecognitionResult(label, bestConfidence)
            }
        } catch (e: Exception) {
            Log.e("SignRecognizer", "Error recognizing image", e)
        }
        
        return null
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

@Composable
actual fun rememberSignRecognizer(): SignRecognizer {
    val context = LocalContext.current
    return remember { SignRecognizer(context) }
}
