package org.ncgroup.kscan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun App() {
    var showScanner by remember { mutableStateOf(false) }
    var barcode by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Add debounce state
    var isShowingSnackbar by remember { mutableStateOf(false) }
    var snackbarJob by remember { mutableStateOf<Job?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
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
                    onFrameOutside = {
                        // Only show snackbar if not already showing
                        if (!isShowingSnackbar) {
                            snackbarJob?.cancel()
                            snackbarJob =
                                scope.launch {
                                    isShowingSnackbar = true
                                    snackbarHostState.showSnackbar(
                                        message = "Please move barcode inside the frame",
                                        duration = SnackbarDuration.Short,
                                    )
                                    // Add delay before allowing next snackbar
                                    delay(1000)
                                    isShowingSnackbar = false
                                }
                        }
                    },
                ) { result ->
                    when (result) {
                        is BarcodeResult.OnSuccess -> {
                            barcode = result.barcode.data
                            format = result.barcode.format
                            showScanner = false
                            snackbarJob?.cancel()
                            isShowingSnackbar = false
                        }
                        is BarcodeResult.OnFailed -> {
                            result.exception.printStackTrace()
                            showScanner = false
                            snackbarJob?.cancel()
                            isShowingSnackbar = false
                        }
                        BarcodeResult.OnCanceled -> {
                            showScanner = false
                            snackbarJob?.cancel()
                            isShowingSnackbar = false
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            snackbarJob?.cancel()
            isShowingSnackbar = false
        }
    }
}

expect fun getPlatformName(): String
