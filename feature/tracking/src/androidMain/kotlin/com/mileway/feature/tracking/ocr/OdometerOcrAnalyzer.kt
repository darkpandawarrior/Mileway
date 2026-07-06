package com.mileway.feature.tracking.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Android ML Kit recognizer actual — thin by design: it only turns one frame ([Uri]) into raw text
 * plus a quality-metrics proxy. All aggregation/consensus/regex-fallback logic lives in the pure
 * commonMain [OcrAggregator] / [FrameQualityAnalyzer], driven by [OdometerOcrOrchestrator].
 */
class MlKitFrameTextRecognizer(private val context: Context) : FrameTextRecognizer<Uri> {
    override suspend fun recognize(frame: Uri): RecognizedFrame =
        withContext(Dispatchers.Default) {
            val inputImage = InputImage.fromFilePath(context, frame)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            try {
                val visionText = recognizer.process(inputImage).await()
                RecognizedFrame(text = visionText.text, metrics = visionText.toQualityMetrics())
            } finally {
                recognizer.close()
            }
        }

    /**
     * ML Kit doesn't expose sharpness/contrast, so those are proxied from text density (more
     * recognized characters per block = a clearer image); textConfidence proxies off block count
     * and average line length since [Text] doesn't surface a numeric confidence score itself.
     * ponytail: density-based proxy, swap for real pixel-level sharpness/contrast if false-accepts
     * show up in the field.
     */
    private fun Text.toQualityMetrics(): FrameQualityAnalyzer.FrameMetrics {
        val lines = textBlocks.flatMap { it.lines }
        if (lines.isEmpty()) return FrameQualityAnalyzer.FrameMetrics(0f, 0f, 0f)
        val avgLineLength = lines.map { it.text.length }.average().toFloat()
        val densityScore = (avgLineLength / 12f).coerceIn(0f, 1f)
        return FrameQualityAnalyzer.FrameMetrics(
            sharpness = densityScore,
            contrast = densityScore,
            textConfidence = densityScore,
        )
    }
}

class OdometerOcrAnalyzer(context: Context) {
    private val orchestrator = OdometerOcrOrchestrator(MlKitFrameTextRecognizer(context))

    /** Single-shot capture — the existing flow, now the N=1 case of [analyzeFrames]. */
    suspend fun analyze(imageUri: Uri): OcrResult = analyzeFrames(listOf(imageUri))

    /** Multi-frame capture: aggregate N frames into one consensus reading. */
    suspend fun analyzeFrames(frameUris: List<Uri>): OcrResult =
        try {
            val aggregate = orchestrator.recognizeFrames(frameUris)
            val reading = aggregate.reading
            if (reading != null) {
                OcrResult.Success(reading = reading, rawText = "agreed by ${aggregate.agreeingFrames}/${aggregate.totalFrames} frames")
            } else {
                OcrResult.Failure("No valid odometer reading across ${frameUris.size} frame(s)")
            }
        } catch (e: Exception) {
            OcrResult.Failure(e.message ?: "Image processing failed")
        }
}

sealed interface OcrResult {
    data class Success(val reading: Int, val rawText: String) : OcrResult

    data class Failure(val reason: String) : OcrResult
}
