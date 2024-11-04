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
    private val frame: Float,
    private val codeTypes: List<BarcodeFormat>,
    private val onSuccess: (List<Barcode>) -> Unit,
    private val onFailed: (Exception) -> Unit,
    private val onCanceled: () -> Unit,
) : ImageAnalysis.Analyzer {
    private val options =
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(getFormatFlags())
            .setZoomSuggestionOptions(
                ZoomSuggestionOptions.Builder(
                    object : ZoomSuggestionOptions.ZoomCallback {
                        override fun setZoom(zoomRatio: Float): Boolean {
                            val cameraControl = camera?.cameraControl
                            val maxZoomRatio =
                                camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1.0f
                            if (zoomRatio <= maxZoomRatio) {
                                cameraControl?.setZoomRatio(zoomRatio)
                                return true
                            } else {
                                return false
                            }
                        }
                    },
                ).setMaxSupportedZoomRatio(5.0f).build(),
            )
            .build()

    private val scanner = BarcodeScanning.getClient(options)

    private val barcodesDetected = mutableMapOf<String, Int>()
    private val barcodesConfirmed = mutableSetOf<Barcode>()

    private fun getFormatFlags(): Int {
        if (codeTypes.isEmpty() || codeTypes.contains(BarcodeFormat.FORMAT_ALL_FORMATS)) {
            return com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS
        }

        return codeTypes.fold(0) { acc, format ->
            acc or
                when (format) {
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
                    BarcodeFormat.FORMAT_ALL_FORMATS -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS
                }
        }
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image).addOnSuccessListener { barcodes ->

                val previewWidth = imageProxy.width.toFloat()
                val previewHeight = imageProxy.height.toFloat()

                val frameLeft = (previewWidth - frame) / 2
                val frameTop = (previewHeight - frame) / 2
                val frameRight = frameLeft + frame
                val frameBottom = frameTop + frame

                for (barcode in barcodes) {
                    val boundingBox = barcode.boundingBox

                    if (!isRequestedFormat(barcode)) continue

                    if (boundingBox != null) {
                        if (boundingBox.left >= frameLeft &&
                            boundingBox.top >= frameTop &&
                            boundingBox.right <= frameRight &&
                            boundingBox.bottom <= frameBottom
                        ) {
                            val displayValue = barcode.displayValue

                            if (displayValue != null) {
                                barcodesDetected[displayValue] =
                                    (barcodesDetected[displayValue] ?: 0) + 1

                                if (requireNotNull(barcodesDetected[displayValue]) >= 2) {
                                    if (!barcodesConfirmed.any { it.data == displayValue }) {
                                        barcodesConfirmed.add(
                                            Barcode(
                                                data = displayValue,
                                                format = getBarcodeFormat(barcode),
                                            ),
                                        )
                                    }
                                }
                            }
                        } else {
                            Log.d("barcode", "barcode is outside the frame.")
                        }
                    }
                }

                val confirmedBarcodes = barcodesConfirmed.toList()

                if (confirmedBarcodes.isNotEmpty()) {
                    onSuccess(confirmedBarcodes)
                    barcodesDetected.clear()
                    barcodesConfirmed.clear()
                }

                imageProxy.close()
            }
                .addOnFailureListener {
                    onFailed(it)
                }
                .addOnCanceledListener {
                    onCanceled()
                    imageProxy.close()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun isRequestedFormat(barcode: com.google.mlkit.vision.barcode.common.Barcode): Boolean {
        if (codeTypes.contains(BarcodeFormat.FORMAT_ALL_FORMATS)) return true

        val format =
            when (barcode.format) {
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
                else -> return false
            }

        return codeTypes.contains(format)
    }

    private fun getBarcodeFormat(barcode: com.google.mlkit.vision.barcode.common.Barcode): String {
        return when (barcode.format) {
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE -> "QR_CODE"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> "CODE_128"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39 -> "CODE_39"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93 -> "CODE_93"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR -> "CODABAR"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> "EAN_13"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> "EAN_8"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF -> "ITF"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> "UPC_A"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> "UPC_E"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417 -> "PDF417"
            com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC -> "AZTEC"
            else -> "UNKNOWN"
        }
    }
}
