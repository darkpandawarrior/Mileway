package com.miletracker.core.platform

// TODO(ios): VisionKit VNDocumentCameraViewController (Phase 4.2)
class IosDocumentScanner : DocumentScanner {
    override suspend fun scan(maxPages: Int): List<ByteArray> = emptyList()
}
