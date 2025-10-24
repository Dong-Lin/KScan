package org.ncgroup.kscan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Sample demonstrating Region of Interest (ROI) feature.
 *
 * This example shows how to restrict barcode scanning to a specific region
 * of the camera preview using [ScanRegion].
 */
@Composable
fun RegionOfInterestUI(modifier: Modifier = Modifier) {
    var showScanner by remember { mutableStateOf(false) }
    var barcode by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("") }

    // Define a centered scan region covering 60% width and 40% height
    val scanRegion = remember {
        ScanRegion.centered(
            width = 0.6f,
            height = 0.4f,
        )
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(text = "Barcode: $barcode")
                Text(text = "Format: $format")
                Button(
                    onClick = { showScanner = true },
                ) {
                    Text(text = "Scan Barcode (ROI)")
                }
                Text(
                    text = "Scanning restricted to center region (60% x 40%)",
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }

            if (showScanner) {
                ScannerView(
                    codeTypes =
                        listOf(
                            BarcodeFormat.FORMAT_QR_CODE,
                            BarcodeFormat.FORMAT_EAN_13,
                            BarcodeFormat.FORMAT_CODE_128,
                        ),
                    showUi = true,
                    scanRegion = scanRegion, // Only detect barcodes in this region
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
            }
        }
    }
}
