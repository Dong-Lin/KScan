package org.ncgroup.kscan

import androidx.compose.runtime.Composable

@Composable
expect fun ScannerView(
    codeTypes: List<BarcodeFormat>,
    colors: ScannerColors = scannerColors(),
    result: (BarcodeResult) -> Unit,
)
