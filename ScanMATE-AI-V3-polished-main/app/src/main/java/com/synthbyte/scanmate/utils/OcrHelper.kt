package com.synthbyte.scanmate.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

data class OcrExtractionResult(
    val text: String,
    val confidencePercent: Int,
    val wordCount: Int,
    val qualityLabel: String
)

object OcrHelper {
    suspend fun extractTextFromBitmap(bitmap: Bitmap, rotationDegrees: Int = 0): String = runTextRecognition {
        InputImage.fromBitmap(bitmap, rotationDegrees)
    }

    suspend fun extractTextFromFile(context: Context, file: File): String = runTextRecognition {
        InputImage.fromFilePath(context, Uri.fromFile(file))
    }

    suspend fun extractTextWithStatsFromFile(context: Context, file: File): OcrExtractionResult {
        val raw = extractTextFromFile(context, file)
        return buildStats(DocumentIntelligence.cleanOcrText(raw))
    }

    fun buildStats(text: String): OcrExtractionResult {
        val clean = DocumentIntelligence.cleanOcrText(text)
        val words = clean.split(Regex("\\s+")).filter { it.isNotBlank() }
        val alphaRatio = clean.count { it.isLetterOrDigit() }.toFloat() / clean.length.coerceAtLeast(1).toFloat()
        val confidence = when {
            clean.isBlank() || clean.startsWith("OCR failed", ignoreCase = true) -> 0
            words.size >= 120 && alphaRatio > 0.62f -> 92
            words.size >= 40 && alphaRatio > 0.54f -> 82
            words.size >= 12 -> 68
            else -> 48
        }
        val label = when {
            confidence >= 88 -> "High confidence"
            confidence >= 72 -> "Good confidence"
            confidence >= 55 -> "Needs review"
            confidence > 0 -> "Low confidence"
            else -> "No OCR text"
        }
        return OcrExtractionResult(clean, confidence, words.size, label)
    }

    private suspend fun runTextRecognition(imageFactory: () -> InputImage): String = suspendCancellableCoroutine { continuation ->
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(imageFactory())
                .addOnSuccessListener { text ->
                    if (continuation.isActive) continuation.resume(text.text.trim())
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume("OCR failed: ${e.localizedMessage ?: "Unknown error"}")
                }
                .addOnCompleteListener {
                    runCatching { recognizer.close() }
                }
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resume("OCR failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
