package com.miletracker.core.platform

// TODO(ios): Vision VNRecognizeTextRequest + VNImageRequestHandler (Phase 4.2)
class IosTextRecognizer : TextRecognizer {
    override suspend fun recognize(imageBytes: ByteArray): String = ""
}
