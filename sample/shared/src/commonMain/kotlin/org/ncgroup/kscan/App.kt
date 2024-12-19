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

@Composable
fun App() {
    var showScanner by remember { mutableStateOf(false) }
    var barcode by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("") }

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

expect fun getPlatformName(): String
