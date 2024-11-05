package org.ncgroup.kscan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import org.ncgroup.kscan.permissions.PermissionsViewModel

@Composable
fun App() {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    val viewModel =
        viewModel {
            PermissionsViewModel(controller = controller)
        }

    var showScanner by remember { mutableStateOf(false) }
    var barcode by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        when (viewModel.state) {
            PermissionState.DeniedAlways -> {
                Column {
                    Text(text = "Permission always denied")
                    Button(
                        onClick = {
                            controller.openAppSettings()
                        },
                    ) {
                        Text(text = "Open settings")
                    }
                }
            }
            PermissionState.Granted -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(text = barcode)
                    Text(text = format)
                    Button(
                        onClick = {
                            showScanner = true
                        },
                    ) {
                        Text(
                            text = "Scan Barcode",
                        )
                    }
                }
                if (showScanner) {
                    ScannerView(
                        codeTypes =
                            listOf(
                                BarcodeFormats.FORMAT_QR_CODE,
                                BarcodeFormats.FORMAT_EAN_13,
                            ),
                    ) { result ->
                        when (result) {
                            is BarcodeResult.OnSuccess -> {
                                barcode = result.barcode.data
                                format = result.barcode.format
                                println("barcode: $barcode, type: ${result.barcode.format}")
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
            else -> {
                Button(
                    onClick = {
                        viewModel.provideOrRequestPermission()
                    },
                ) {
                    Text(text = "Request permission")
                }
            }
        }
    }
}

expect fun getPlatformName(): String
