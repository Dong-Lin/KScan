package org.ncgroup.kscan

import androidx.compose.runtime.Composable

@Composable
actual fun ScannerView(
    codeTypes: List<BarcodeFormat>,
    colors: ScannerColors,
    result: (BarcodeResult) -> Unit,
) {}
