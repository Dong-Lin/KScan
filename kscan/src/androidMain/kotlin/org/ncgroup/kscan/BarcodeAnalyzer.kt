package org.ncgroup.kscan

import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UNKNOWN
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val camera: Camera?,
    private val codeTypes: List<BarcodeFormat>,
    private val onSuccess: (List<Barcode>) -> Unit,
    private val onFailed: (Exception) -> Unit,
    private val onCanceled: () -> Unit,
) : ImageAnalysis.Analyzer {
    private val scannerOptions =
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(getMLKitBarcodeFormats(codeTypes))
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

    private val scanner = BarcodeScanning.getClient(scannerOptions)
    private val barcodesDetected = mutableMapOf<String, Int>()
    private var hasSuccessfullyProcessedBarcode = false //

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (hasSuccessfullyProcessedBarcode) {
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
                val relevantBarcodes = barcodes.filter { isRequestedFormat(it) }
                if (relevantBarcodes.isNotEmpty()) {
                    processFoundBarcodes(relevantBarcodes)
                }
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

    private fun processFoundBarcodes(mlKitBarcodes: List<com.google.mlkit.vision.barcode.common.Barcode>) {
        if (hasSuccessfullyProcessedBarcode) return

        for (mlKitBarcode in mlKitBarcodes) {
            val displayValue = mlKitBarcode.displayValue ?: continue

            barcodesDetected[displayValue] = (barcodesDetected[displayValue] ?: 0) + 1
            if ((barcodesDetected[displayValue] ?: 0) >= 2) {
                val appSpecificFormat = mlKitFormatToAppFormat(mlKitBarcode.format)
                val detectedAppBarcode =
                    Barcode(
                        data = displayValue,
                        format = appSpecificFormat.toString(),
                    )
                onSuccess(listOf(detectedAppBarcode))
                barcodesDetected.clear()
                hasSuccessfullyProcessedBarcode = true
                break
            }
        }
    }

    private fun isRequestedFormat(mlKitBarcode: com.google.mlkit.vision.barcode.common.Barcode): Boolean {
        if (codeTypes.contains(BarcodeFormat.FORMAT_ALL_FORMATS)) {
            return MLKIT_TO_APP_FORMAT_MAP.containsKey(mlKitBarcode.format)
        }
        val appFormat = mlKitFormatToAppFormat(mlKitBarcode.format)
        return codeTypes.contains(appFormat)
    }

    companion object {
        private val APP_TO_MLKIT_FORMAT_MAP: Map<BarcodeFormat, Int> =
            mapOf(
                BarcodeFormat.FORMAT_QR_CODE to FORMAT_QR_CODE,
                BarcodeFormat.FORMAT_CODE_128 to FORMAT_CODE_128,
                BarcodeFormat.FORMAT_CODE_39 to FORMAT_CODE_39,
                BarcodeFormat.FORMAT_CODE_93 to FORMAT_CODE_93,
                BarcodeFormat.FORMAT_CODABAR to FORMAT_CODABAR,
                BarcodeFormat.FORMAT_DATA_MATRIX to FORMAT_DATA_MATRIX,
                BarcodeFormat.FORMAT_EAN_13 to FORMAT_EAN_13,
                BarcodeFormat.FORMAT_EAN_8 to FORMAT_EAN_8,
                BarcodeFormat.FORMAT_ITF to FORMAT_ITF,
                BarcodeFormat.FORMAT_UPC_A to FORMAT_UPC_A,
                BarcodeFormat.FORMAT_UPC_E to FORMAT_UPC_E,
                BarcodeFormat.FORMAT_PDF417 to FORMAT_PDF417,
                BarcodeFormat.FORMAT_AZTEC to FORMAT_AZTEC,
            )

        private val MLKIT_TO_APP_FORMAT_MAP: Map<Int, BarcodeFormat> =
            APP_TO_MLKIT_FORMAT_MAP.entries.associateBy({ it.value }) { it.key }
                .plus(FORMAT_UNKNOWN to BarcodeFormat.TYPE_UNKNOWN)

        fun getMLKitBarcodeFormats(appFormats: List<BarcodeFormat>): Int {
            if (appFormats.isEmpty() || appFormats.contains(BarcodeFormat.FORMAT_ALL_FORMATS)) {
                return FORMAT_ALL_FORMATS
            }

            return appFormats
                .mapNotNull { APP_TO_MLKIT_FORMAT_MAP[it] }
                .distinct()
                .fold(0) { acc, formatInt -> acc or formatInt }
                .let { if (it == 0) FORMAT_ALL_FORMATS else it }
        }

        fun mlKitFormatToAppFormat(mlKitFormat: Int): BarcodeFormat {
            return MLKIT_TO_APP_FORMAT_MAP[mlKitFormat] ?: BarcodeFormat.TYPE_UNKNOWN
        }
    }
}
