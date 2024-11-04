package org.ncgroup.kscan

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectType
import platform.AVFoundation.AVMetadataObjectTypeAztecCode
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeCode93Code
import platform.AVFoundation.AVMetadataObjectTypeDataMatrixCode
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypePDF417Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import platform.AVFoundation.videoZoomFactor
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UIViewController
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
class CameraViewController(
    private val frame: Float,
    private val device: AVCaptureDevice,
    private val codeTypes: List<BarcodeFormat>,
    private val onBarcodeSuccess: (List<Barcode>) -> Unit,
    private val onBarcodeFailed: (Exception) -> Unit,
    private val onBarcodeCanceled: () -> Unit,
    private val onMaxZoomRatioAvailable: (Float) -> Unit,
) : UIViewController(null, null), AVCaptureMetadataOutputObjectsDelegateProtocol {
    private lateinit var captureSession: AVCaptureSession
    private lateinit var previewLayer: AVCaptureVideoPreviewLayer
    private lateinit var videoInput: AVCaptureDeviceInput

    private val barcodesDetected = mutableMapOf<String, Int>()
    private val barcodesConfirmed = mutableSetOf<Barcode>()

    private var scanFrame = CGRectMake(0.0, 0.0, 0.0, 0.0)

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.blackColor
        setupCamera()
        onMaxZoomRatioAvailable(device.activeFormat.videoMaxZoomFactor.toFloat())
    }

    private fun getMetadataObjectTypes(): List<AVMetadataObjectType> {
        if (codeTypes.isEmpty() || codeTypes.contains(BarcodeFormat.FORMAT_ALL_FORMATS)) {
            return listOf(
                AVMetadataObjectTypeQRCode,
                AVMetadataObjectTypeEAN13Code,
                AVMetadataObjectTypeEAN8Code,
                AVMetadataObjectTypeCode128Code,
                AVMetadataObjectTypeCode39Code,
                AVMetadataObjectTypeCode93Code,
                AVMetadataObjectTypeUPCECode,
                AVMetadataObjectTypePDF417Code,
                AVMetadataObjectTypeAztecCode,
                AVMetadataObjectTypeDataMatrixCode,
            )
        }

        return codeTypes.mapNotNull { format ->
            when (format) {
                BarcodeFormat.FORMAT_QR_CODE -> AVMetadataObjectTypeQRCode
                BarcodeFormat.FORMAT_EAN_13 -> AVMetadataObjectTypeEAN13Code
                BarcodeFormat.FORMAT_EAN_8 -> AVMetadataObjectTypeEAN8Code
                BarcodeFormat.FORMAT_CODE_128 -> AVMetadataObjectTypeCode128Code
                BarcodeFormat.FORMAT_CODE_39 -> AVMetadataObjectTypeCode39Code
                BarcodeFormat.FORMAT_CODE_93 -> AVMetadataObjectTypeCode93Code
                BarcodeFormat.FORMAT_UPC_E -> AVMetadataObjectTypeUPCECode
                BarcodeFormat.FORMAT_PDF417 -> AVMetadataObjectTypePDF417Code
                BarcodeFormat.FORMAT_AZTEC -> AVMetadataObjectTypeAztecCode
                BarcodeFormat.FORMAT_DATA_MATRIX -> AVMetadataObjectTypeDataMatrixCode
                else -> null
            }
        }
    }

    private fun setupCamera() {
        captureSession = AVCaptureSession()
        val metadataOutput = AVCaptureMetadataOutput()

        try {
            videoInput = AVCaptureDeviceInput.deviceInputWithDevice(device, null) as AVCaptureDeviceInput
        } catch (e: Exception) {
            onBarcodeFailed(e)
            return
        }

        if (captureSession.canAddInput(videoInput)) {
            captureSession.addInput(videoInput)
        } else {
            onBarcodeFailed(Exception("Failed to add video input"))
            return
        }

        if (captureSession.canAddOutput(metadataOutput)) {
            captureSession.addOutput(metadataOutput)
            metadataOutput.setMetadataObjectsDelegate(this, dispatch_get_main_queue())

            // Set the metadata object types based on requested code types
            val supportedTypes = getMetadataObjectTypes()
            if (supportedTypes.isEmpty()) {
                onBarcodeFailed(Exception("No supported barcode types selected"))
                return
            }
            metadataOutput.metadataObjectTypes = supportedTypes
        } else {
            onBarcodeFailed(Exception("Failed to add metadata output"))
            return
        }

        previewLayer = AVCaptureVideoPreviewLayer.layerWithSession(captureSession)
        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        view.layer.addSublayer(previewLayer)

        setupScanningArea(metadataOutput)

        captureSession.startRunning()
    }

    private fun setupScanningArea(metadataOutput: AVCaptureMetadataOutput) {
        val viewWidth = view.frame.useContents { size.width }
        val viewHeight = view.frame.useContents { size.height }

        scanFrame =
            CGRectMake(
                x = (viewWidth - frame) / 2,
                y = (viewHeight - frame) / 2,
                width = frame.toDouble(),
                height = frame.toDouble(),
            )

        val interest = previewLayer.metadataOutputRectOfInterestForRect(scanFrame)
        metadataOutput.rectOfInterest = interest
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        if (!captureSession.isRunning()) {
            captureSession.startRunning()
        }
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        if (captureSession.isRunning()) {
            captureSession.stopRunning()
        }
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        previewLayer.frame = view.layer.bounds
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        for (metadataObject in didOutputMetadataObjects) {
            if (metadataObject !is AVMetadataMachineReadableCodeObject) continue

            val barcodeObject =
                previewLayer.transformedMetadataObjectForMetadataObject(metadataObject)
                    as? AVMetadataMachineReadableCodeObject ?: continue

            // Check if the barcode type is in the requested formats
            if (!isRequestedFormat(barcodeObject.type)) continue

            val bounds = barcodeObject.bounds

            val boundsX = bounds.useContents { this.origin.x }
            val boundsY = bounds.useContents { this.origin.y }
            val boundsWidth = bounds.useContents { this.size.width }
            val boundsHeight = bounds.useContents { this.size.height }

            val scanFrameX = scanFrame.useContents { this.origin.x }
            val scanFrameY = scanFrame.useContents { this.origin.y }
            val scanFrameWidth = scanFrame.useContents { this.size.width }
            val scanFrameHeight = scanFrame.useContents { this.size.height }

            if (boundsX < scanFrameX ||
                boundsY < scanFrameY ||
                boundsX + boundsWidth > scanFrameX + scanFrameWidth ||
                boundsY + boundsHeight > scanFrameY + scanFrameHeight
            ) {
                continue
            }

            val stringValue = barcodeObject.stringValue ?: continue

            barcodesDetected[stringValue] = (barcodesDetected[stringValue] ?: 0) + 1

            if (requireNotNull(barcodesDetected[stringValue]) >= 2) {
                if (!barcodesConfirmed.any { it.data == stringValue }) {
                    barcodesConfirmed.add(
                        Barcode(
                            data = stringValue,
                            format = getBarcodeFormat(barcodeObject.type),
                        ),
                    )
                }
            }
        }

        val confirmedBarcodes = barcodesConfirmed.toList()
        if (confirmedBarcodes.isNotEmpty()) {
            onBarcodeSuccess(confirmedBarcodes)
            barcodesDetected.clear()
            barcodesConfirmed.clear()
            captureSession.stopRunning()
        }
    }

    private fun isRequestedFormat(type: AVMetadataObjectType): Boolean {
        if (codeTypes.contains(BarcodeFormat.FORMAT_ALL_FORMATS)) return true

        val format =
            when (type) {
                AVMetadataObjectTypeQRCode -> BarcodeFormat.FORMAT_QR_CODE
                AVMetadataObjectTypeEAN13Code -> BarcodeFormat.FORMAT_EAN_13
                AVMetadataObjectTypeEAN8Code -> BarcodeFormat.FORMAT_EAN_8
                AVMetadataObjectTypeCode128Code -> BarcodeFormat.FORMAT_CODE_128
                AVMetadataObjectTypeCode39Code -> BarcodeFormat.FORMAT_CODE_39
                AVMetadataObjectTypeCode93Code -> BarcodeFormat.FORMAT_CODE_93
                AVMetadataObjectTypeUPCECode -> BarcodeFormat.FORMAT_UPC_E
                AVMetadataObjectTypePDF417Code -> BarcodeFormat.FORMAT_PDF417
                AVMetadataObjectTypeAztecCode -> BarcodeFormat.FORMAT_AZTEC
                AVMetadataObjectTypeDataMatrixCode -> BarcodeFormat.FORMAT_DATA_MATRIX
                else -> return false
            }

        return codeTypes.contains(format)
    }

    private fun getBarcodeFormat(type: AVMetadataObjectType): String =
        when (type) {
            AVMetadataObjectTypeQRCode -> "QR_CODE"
            AVMetadataObjectTypeEAN13Code -> "EAN_13"
            AVMetadataObjectTypeEAN8Code -> "EAN_8"
            AVMetadataObjectTypeCode128Code -> "CODE_128"
            AVMetadataObjectTypeCode39Code -> "CODE_39"
            AVMetadataObjectTypeCode93Code -> "CODE_93"
            AVMetadataObjectTypeUPCECode -> "UPC_E"
            AVMetadataObjectTypePDF417Code -> "PDF417"
            AVMetadataObjectTypeAztecCode -> "AZTEC"
            AVMetadataObjectTypeDataMatrixCode -> "DATA_MATRIX"
            else -> "UNKNOWN"
        }

    fun setZoom(ratio: Float) {
        try {
            device.lockForConfiguration(null)
            device.videoZoomFactor =
                ratio.toDouble().coerceIn(
                    1.0,
                    device.activeFormat.videoMaxZoomFactor,
                )
            device.unlockForConfiguration()
        } catch (e: Exception) {
            print("Failed to update zoom: ${e.message}")
        }
    }
}
