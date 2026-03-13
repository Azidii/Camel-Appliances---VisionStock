package com.example.visionstock.scanner

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Result from a single classification run.
 */
data class ClassificationResult(val label: String, val confidence: Float, val category: String)

/**
 * Helper class that loads the camelai_float32.tflite model and labels,
 * preprocesses a Bitmap, runs inference, and returns the top prediction.
 *
 * Supports YOLOv8 detection models with output shape [1, N, 8400]
 * where N = 4 (bbox) + numClasses.
 */
class CamelAIClassifier(context: Context) {

    companion object {
        private const val TAG = "CamelAIClassifier"
        private val categoryMap = mapOf(
            "RMC" to "Raw Materials",
            "FMC" to "Raw Materials",
            "Base" to "Raw Materials",
            "Post" to "Raw Materials"
        )
    }

    private val interpreter: Interpreter
    private val labels: List<String>
    private val inputSize: Int

    // Output tensor metadata
    private val outputRows: Int   // e.g. 8 (4 bbox + 4 classes)
    private val outputCols: Int   // e.g. 8400 (anchor boxes)

    init {
        // 1. Load TFLite model
        val model = loadModelFile(context, "camelai_float32.tflite")
        interpreter = Interpreter(model)

        // 2. Input dimensions
        val inputShape = interpreter.getInputTensor(0).shape() // [1, H, W, 3]
        inputSize = inputShape[1]
        Log.d(TAG, "Model input shape: ${inputShape.contentToString()}")

        // 3. Output dimensions — read dynamically from the model
        val outputShape = interpreter.getOutputTensor(0).shape() // e.g. [1, 8, 8400]
        outputRows = outputShape[1]
        outputCols = outputShape[2]
        Log.d(TAG, "Model output shape: ${outputShape.contentToString()} (rows=$outputRows, cols=$outputCols)")

        // 4. Load labels
        labels = loadLabels(context, "labels.txt")
        Log.d(TAG, "Loaded ${labels.size} labels: $labels")
    }

    /**
     * Classify a Bitmap and return the top-1 prediction.
     */
    fun classify(bitmap: Bitmap): ClassificationResult {
        // 1. Convert to ARGB_8888
        val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true) ?: bitmap

        // 2. Center crop
        val minDim = Math.min(softwareBitmap.width, softwareBitmap.height)
        val startX = (softwareBitmap.width - minDim) / 2
        val startY = (softwareBitmap.height - minDim) / 2
        val cropped = Bitmap.createBitmap(softwareBitmap, startX, startY, minDim, minDim)

        // 3. Resize
        val resized = Bitmap.createScaledBitmap(cropped, inputSize, inputSize, true)

        // 4. Convert to input ByteBuffer
        val inputBuffer = bitmapToByteBuffer(resized)

        // 5. Allocate output as a flat ByteBuffer (most compatible with TFLite)
        // Shape [1, outputRows, outputCols] → total floats = outputRows * outputCols
        val totalOutputFloats = outputRows * outputCols
        val outputBuffer = ByteBuffer.allocateDirect(totalOutputFloats * 4)
        outputBuffer.order(ByteOrder.nativeOrder())

        // 6. Run inference
        interpreter.run(inputBuffer, outputBuffer)

        // 7. Parse the flat output buffer
        // Layout: [1, rows, cols] stored row-major
        // Row 0-3 = bbox (x, y, w, h), Row 4+ = class probabilities
        outputBuffer.rewind()
        val outputData = FloatArray(totalOutputFloats)
        outputBuffer.asFloatBuffer().get(outputData)

        // Helper to read: outputData[row * outputCols + col]
        val numClasses = labels.size
        val bboxRows = outputRows - numClasses  // typically 4 (x, y, w, h)

        var maxClassIdx = 0
        var maxProb = 0.0f

        for (boxIdx in 0 until outputCols) {
            for (classIdx in 0 until numClasses) {
                val row = bboxRows + classIdx  // class rows start after bbox rows
                val prob = outputData[row * outputCols + boxIdx]
                if (prob > maxProb) {
                    maxProb = prob
                    maxClassIdx = classIdx
                }
            }
        }

        val predictedLabel = if (maxClassIdx < labels.size) labels[maxClassIdx] else "Unknown"
        val predictedCategory = categoryMap[predictedLabel] ?: "Raw Materials"

        Log.d(TAG, "Result: label=$predictedLabel, confidence=$maxProb, category=$predictedCategory")

        return ClassificationResult(
            label = predictedLabel,
            confidence = maxProb,
            category = predictedCategory
        )
    }

    fun close() {
        interpreter.close()
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabels(context: Context, filename: String): List<String> {
        val labels = mutableListOf<String>()
        val reader = BufferedReader(InputStreamReader(context.assets.open(filename)))
        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    labels.add(trimmed)
                }
            }
        }
        return labels
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f) // R
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)  // G
            byteBuffer.putFloat((pixel and 0xFF) / 255.0f)          // B
        }

        return byteBuffer
    }
}
