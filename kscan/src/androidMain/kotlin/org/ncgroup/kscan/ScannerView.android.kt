package org.ncgroup.kscan

import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
actual fun ScannerView(
    modifier: Modifier,
    codeTypes: List<BarcodeFormat>,
    colors: ScannerColors,
    showUi: Boolean,
    result: (BarcodeResult) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture =
        remember {
            ProcessCameraProvider.getInstance(context)
                .get()
        }

    var camera: Camera? by remember { mutableStateOf(null) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }

    var torchEnabled by remember { mutableStateOf(false) }
    var zoomRatio by remember { mutableStateOf(1f) }
    var maxZoomRatio by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        camera?.cameraInfo?.torchState?.observe(lifecycleOwner) {
            torchEnabled = it == TorchState.ON
        }
    }

    LaunchedEffect(Unit) {
        camera?.cameraInfo?.zoomState?.observe(lifecycleOwner) {
            zoomRatio = it.zoomRatio
            maxZoomRatio = it.maxZoomRatio
        }
    }

    Box(
        modifier = modifier,
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->

                val previewView = PreviewView(context)
                val preview = Preview.Builder().build()
                val selector =
                    CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                preview.surfaceProvider = previewView.surfaceProvider

                val imageAnalysis =
                    ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    BarcodeAnalyzer(
                        camera = camera,
                        codeTypes = codeTypes,
                        onSuccess = { scannedBarcodes ->
                            result(BarcodeResult.OnSuccess(scannedBarcodes.first()))
                            cameraProviderFuture.unbind(imageAnalysis)
                        },
                        onFailed = { result(BarcodeResult.OnFailed(Exception(it))) },
                        onCanceled = { result(BarcodeResult.OnCanceled) },
                    ),
                )

                camera =
                    bindCamera(
                        lifecycleOwner = lifecycleOwner,
                        cameraProviderFuture = cameraProviderFuture,
                        selector = selector,
                        preview = preview,
                        imageAnalysis = imageAnalysis,
                        result = result,
                        cameraControl = { cameraControl = it },
                    )

                previewView
            },
            onRelease = {
                cameraProviderFuture.unbind()
            },
        )

        if (showUi) {
            ScannerUI(
                onCancel = { result(BarcodeResult.OnCanceled) },
                torchEnabled = torchEnabled,
                onTorchEnabled = { cameraControl?.enableTorch(it) },
                zoomRatio = zoomRatio,
                zoomRatioOnChange = { ratio ->
                    cameraControl?.setZoomRatio(ratio)
                },
                maxZoomRatio = maxZoomRatio,
                colors = colors,
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProviderFuture.unbindAll()
            camera = null
            cameraControl = null
        }
    }
}

internal fun bindCamera(
    lifecycleOwner: LifecycleOwner,
    cameraProviderFuture: ProcessCameraProvider?,
    selector: CameraSelector,
    preview: Preview,
    imageAnalysis: ImageAnalysis,
    result: (BarcodeResult) -> Unit,
    cameraControl: (CameraControl?) -> Unit,
): Camera? {
    return runCatching {
        cameraProviderFuture?.unbindAll()
        cameraProviderFuture?.bindToLifecycle(
            lifecycleOwner,
            selector,
            preview,
            imageAnalysis,
        ).also {
            cameraControl(it?.cameraControl)
        }
    }.getOrElse {
        result(BarcodeResult.OnFailed(Exception(it)))
        null
    }
}
