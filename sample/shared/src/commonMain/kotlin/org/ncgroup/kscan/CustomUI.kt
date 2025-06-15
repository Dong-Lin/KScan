package org.ncgroup.kscan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomUI(modifier: Modifier = Modifier) {
    var showScanner by remember { mutableStateOf(false) }
    var barcode by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("") }

    var scannerController = remember { ScannerController() }

    Scaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(text = barcode)
                Text(text = format)
                Button(
                    onClick = { showScanner = true },
                ) {
                    Text(text = "Scan Barcode")
                }
            }

            if (showScanner) {
                ScannerView(
                    codeTypes =
                        listOf(
                            BarcodeFormat.FORMAT_ALL_FORMATS,
                        ),
                    showUi = false,
                    scannerController = scannerController,
                ) { result ->
                    when (result) {
                        is BarcodeResult.OnSuccess -> {
                            barcode = result.barcode.data
                            format = result.barcode.format
                            showScanner = false
                        }
                        is BarcodeResult.OnFailed -> {
                            result.exception.printStackTrace()
                            showScanner = false
                        }
                        BarcodeResult.OnCanceled -> {
                            showScanner = false
                        }
                    }
                }
                Box(modifier = modifier.fillMaxSize()) {
                    Column(
                        modifier =
                            modifier
                                .padding(bottom = 24.dp)
                                .align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            onClick = {
                                scannerController.setTorch(!scannerController.torchEnabled)
                            },
                        ) {
                            Text(text = "Torch ${if (scannerController.torchEnabled) "On" else "Off"}")
                        }
                        ZoomSlider(
                            zoomRatio = scannerController.zoomRatio,
                            onZoomChange = { ratio ->
                                scannerController.setZoom(ratio)
                            },
                            maxZoomRatio = scannerController.maxZoomRatio,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomSlider(
    zoomRatio: Float,
    onZoomChange: (Float) -> Unit,
    maxZoomRatio: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Zoom: $zoomRatio")
        Slider(
            value = zoomRatio,
            onValueChange = onZoomChange,
            valueRange = 1f..maxZoomRatio,
            steps = maxOf(0, (maxZoomRatio - 1f).toInt() - 1),
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}
