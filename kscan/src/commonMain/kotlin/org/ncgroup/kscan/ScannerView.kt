package org.ncgroup.kscan

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ScannerView(
    modifier: Modifier = Modifier.fillMaxSize(),
    codeTypes: List<BarcodeFormat>,
    colors: ScannerColors = scannerColors(),
    showUi: Boolean = true,
    scannerController: ScannerController? = null,
    result: (BarcodeResult) -> Unit,
)
