package com.mileway.feature.tracking.ocr

/**
 * iOS recognizer actual for the multi-frame OCR boundary (see [FrameTextRecognizer] in commonMain).
 * `T` is a `String` file-path handle here — the iOS equivalent of Android's `content://` [android.net.Uri].
 *
 * ponytail: stub only — no `VNRecognizeTextRequest`/Vision cinterop wired yet since this task is
 * scoped to the pure commonMain orchestration (quality scoring + aggregation + regex fallback), not
 * a camera capture pipeline. Wiring real Vision text recognition here is a same-shape follow-up to
 * [com.mileway.feature.tracking.ocr.MlKitFrameTextRecognizer.recognize] — decode the file at `frame`,
 * run `VNImageRequestHandler` + `VNRecognizeTextRequest`, map `VNRecognizedTextObservation.confidence`
 * into [FrameQualityAnalyzer.FrameMetrics.textConfidence]. Until then this keeps `:feature:tracking`
 * building for the iOS target with the same commonMain decision logic Android already exercises.
 */
class VisionFrameTextRecognizer : FrameTextRecognizer<String> {
    override suspend fun recognize(frame: String): RecognizedFrame {
        return RecognizedFrame(text = "", metrics = FrameQualityAnalyzer.FrameMetrics(0f, 0f, 0f))
    }
}
