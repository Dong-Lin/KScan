package org.ncgroup.kscan

@androidx.compose.runtime.Composable
actual fun ScannerView(
    modifier: androidx.compose.ui.Modifier,
    codeTypes: List<org.ncgroup.kscan.BarcodeFormat>,
    colors: org.ncgroup.kscan.ScannerColors,
    showUi: Boolean,
    scannerController: org.ncgroup.kscan.ScannerController?,
    filter: (org.ncgroup.kscan.Barcode) -> Boolean,
    result: (org.ncgroup.kscan.BarcodeResult) -> Unit,
) {
}
