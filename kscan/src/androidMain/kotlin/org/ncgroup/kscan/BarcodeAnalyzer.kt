package org.ncgroup.kscan

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val camera: Camera?,
    private val codeTypes: List<BarcodeFormat>,
    private val onSuccess: (List<Barcode>) -> Unit,
    private val onFailed: (Exception) -> Unit,
    private val onCanceled: () -> Unit,
) : ImageAnalysis.Analyzer {
    private val options =
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                if (codeTypes.isEmpty() || codeTypes.contains(BarcodeFormat.FORMAT_ALL_FORMATS)) {
                    com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS
                } else {
                    codeTypes
                        .filter { it.name.startsWith("FORMAT_") && it != BarcodeFormat.FORMAT_ALL_FORMATS }
                        .map { it.toMLKitFormat() }
                        .fold(0) { acc, format -> acc or format }
                },
            )
            .setZoomSuggestionOptions(
                ZoomSuggestionOptions.Builder { zoomRatio ->
                    val maxZoomRatio =
                        (camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1.0f)
                            .coerceAtMost(5.0f)
                    if (zoomRatio <= maxZoomRatio) {
                        camera?.cameraControl?.setZoomRatio(zoomRatio)
                        true
                    } else {
                        false
                    }
                }.setMaxSupportedZoomRatio(5.0f).build(),
            )
            .build()

    private val scanner = BarcodeScanning.getClient(options)
    private val barcodesDetected = mutableMapOf<String, Int>()
    private var hasDetectedBarcode = false

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (hasDetectedBarcode) {
            imageProxy.close()
            return
        }

        val mediaImage =
            imageProxy.image ?: run {
                imageProxy.close()
                return
            }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                processBarcodes(barcodes)
            }
            .addOnFailureListener {
                onFailed(it)
                imageProxy.close()
            }
            .addOnCanceledListener {
                onCanceled()
                imageProxy.close()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun processBarcodes(barcodes: List<com.google.mlkit.vision.barcode.common.Barcode>) {
        for (barcode in barcodes) {
            if (!isRequestedFormat(barcode)) {
                Log.d("barcode_flow", "Format check failed")
                continue
            }
            processDetectedBarcode(barcode.displayValue.orEmpty(), barcode)
        }
    }

    private fun processDetectedBarcode(
        displayValue: String,
        barcode: com.google.mlkit.vision.barcode.common.Barcode,
    ) {
        barcodesDetected[displayValue] = (barcodesDetected[displayValue] ?: 0) + 1
        if (requireNotNull(barcodesDetected[displayValue]) >= 2) {
            val detectedBarcode =
                Barcode(
                    data = displayValue,
                    format = barcode.toFormat().toString(),
                )
            onSuccess(listOf(detectedBarcode))
            barcodesDetected.clear()
            hasDetectedBarcode = true
        }
    }

    private fun isRequestedFormat(barcode: com.google.mlkit.vision.barcode.common.Barcode): Boolean {
        return codeTypes.contains(BarcodeFormat.FORMAT_ALL_FORMATS) ||
            codeTypes.contains(barcode.toFormat())
    }
}

private fun BarcodeFormat.toMLKitFormat(): Int =
    when (this) {
        BarcodeFormat.FORMAT_QR_CODE -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
        BarcodeFormat.FORMAT_CODE_128 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128
        BarcodeFormat.FORMAT_CODE_39 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39
        BarcodeFormat.FORMAT_CODE_93 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93
        BarcodeFormat.FORMAT_CODABAR -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR
        BarcodeFormat.FORMAT_DATA_MATRIX -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX
        BarcodeFormat.FORMAT_EAN_13 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13
        BarcodeFormat.FORMAT_EAN_8 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8
        BarcodeFormat.FORMAT_ITF -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF
        BarcodeFormat.FORMAT_UPC_A -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A
        BarcodeFormat.FORMAT_UPC_E -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E
        BarcodeFormat.FORMAT_PDF417 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417
        BarcodeFormat.FORMAT_AZTEC -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC
        else -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS
    }

private fun com.google.mlkit.vision.barcode.common.Barcode.toFormat(): BarcodeFormat =
    when (format) {
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE -> BarcodeFormat.FORMAT_QR_CODE
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> BarcodeFormat.FORMAT_CODE_128
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39 -> BarcodeFormat.FORMAT_CODE_39
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93 -> BarcodeFormat.FORMAT_CODE_93
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR -> BarcodeFormat.FORMAT_CODABAR
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.FORMAT_DATA_MATRIX
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> BarcodeFormat.FORMAT_EAN_13
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> BarcodeFormat.FORMAT_EAN_8
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF -> BarcodeFormat.FORMAT_ITF
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> BarcodeFormat.FORMAT_UPC_A
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> BarcodeFormat.FORMAT_UPC_E
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417 -> BarcodeFormat.FORMAT_PDF417
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC -> BarcodeFormat.FORMAT_AZTEC
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UNKNOWN -> BarcodeFormat.TYPE_UNKNOWN
        else -> BarcodeFormat.TYPE_UNKNOWN
    }
