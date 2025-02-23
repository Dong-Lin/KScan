package org.ncgroup.kscan

import kotlinx.cinterop.ExperimentalForeignApi
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
import platform.UIKit.UIColor
import platform.UIKit.UIViewController
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
class CameraViewController(
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

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.blackColor
        setupCamera()
        onMaxZoomRatioAvailable(device.activeFormat.videoMaxZoomFactor.toFloat().coerceAtMost(5.0f))
    }

    private fun setupCamera() {
        captureSession = AVCaptureSession()

        try {
            videoInput = AVCaptureDeviceInput.deviceInputWithDevice(device, null) as AVCaptureDeviceInput
        } catch (e: Exception) {
            onBarcodeFailed(e)
            return
        }

        setupCaptureSession()
    }

    private fun setupCaptureSession() {
        val metadataOutput = AVCaptureMetadataOutput()

        if (!captureSession.canAddInput(videoInput)) {
            onBarcodeFailed(Exception("Failed to add video input"))
            return
        }
        captureSession.addInput(videoInput)

        if (!captureSession.canAddOutput(metadataOutput)) {
            onBarcodeFailed(Exception("Failed to add metadata output"))
            return
        }
        captureSession.addOutput(metadataOutput)

        setupMetadataOutput(metadataOutput)
        setupPreviewLayer()
        captureSession.startRunning()
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

    private fun setupMetadataOutput(metadataOutput: AVCaptureMetadataOutput) {
        metadataOutput.setMetadataObjectsDelegate(this, dispatch_get_main_queue())

        val supportedTypes = getMetadataObjectTypes()
        if (supportedTypes.isEmpty()) {
            onBarcodeFailed(Exception("No supported barcode types selected"))
            return
        }
        metadataOutput.metadataObjectTypes = supportedTypes
    }

    private fun setupPreviewLayer() {
        previewLayer = AVCaptureVideoPreviewLayer.layerWithSession(captureSession)
        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        view.layer.addSublayer(previewLayer)
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
        processBarcodes(didOutputMetadataObjects)
    }

    private fun processBarcodes(metadataObjects: List<*>) {
        for (metadataObject in metadataObjects) {
            if (metadataObject !is AVMetadataMachineReadableCodeObject) continue
            val barcodeObject =
                previewLayer.transformedMetadataObjectForMetadataObject(metadataObject)
                    as? AVMetadataMachineReadableCodeObject ?: continue

            if (!isRequestedFormat(barcodeObject.type)) continue
            val stringValue = barcodeObject.stringValue ?: continue
            processDetectedBarcode(stringValue, barcodeObject.type)
        }
        checkConfirmedBarcodes()
    }

    private fun processDetectedBarcode(
        value: String,
        type: AVMetadataObjectType,
    ) {
        barcodesDetected[value] = (barcodesDetected[value] ?: 0) + 1

        if ((barcodesDetected[value] ?: 0) >= 2) {
            if (!barcodesConfirmed.any { it.data == value }) {
                barcodesConfirmed.add(
                    Barcode(
                        data = value,
                        format = type.toFormat().toString(),
                    ),
                )
            }
        }
    }

    private fun checkConfirmedBarcodes() {
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

    private fun AVMetadataObjectType.toFormat(): BarcodeFormat =
        when (this) {
            AVMetadataObjectTypeQRCode -> BarcodeFormats.FORMAT_QR_CODE
            AVMetadataObjectTypeEAN13Code -> BarcodeFormats.FORMAT_EAN_13
            AVMetadataObjectTypeEAN8Code -> BarcodeFormats.FORMAT_EAN_8
            AVMetadataObjectTypeCode128Code -> BarcodeFormats.FORMAT_CODE_128
            AVMetadataObjectTypeCode39Code -> BarcodeFormats.FORMAT_CODE_39
            AVMetadataObjectTypeCode93Code -> BarcodeFormats.FORMAT_CODE_93
            AVMetadataObjectTypeUPCECode -> BarcodeFormats.FORMAT_UPC_E
            AVMetadataObjectTypePDF417Code -> BarcodeFormats.FORMAT_PDF417
            AVMetadataObjectTypeAztecCode -> BarcodeFormats.FORMAT_AZTEC
            AVMetadataObjectTypeDataMatrixCode -> BarcodeFormats.FORMAT_DATA_MATRIX
            else -> BarcodeFormats.TYPE_UNKNOWN
        }

    fun setZoom(ratio: Float) {
        try {
            device.lockForConfiguration(null)
            val maxZoom = device.activeFormat.videoMaxZoomFactor.toFloat().coerceAtMost(5.0f)
            device.videoZoomFactor = ratio.toDouble().coerceIn(1.0, maxZoom.toDouble())
            device.unlockForConfiguration()
        } catch (e: Exception) {
            print("Failed to update zoom: ${e.message}")
        }
    }
}
